package seedu.command;

import seedu.calendar.CalendarParser;
import seedu.exception.ProjException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

import static seedu.common.Constants.TAB;


import seedu.tasks.Class;
import seedu.tasks.TaskNonclass;
import seedu.ui.Ui;
import seedu.tasks.Task;

public class ListCommand extends Command {

    public static final int TASKLIST_OFFSET = 1;
    private String userInput;


    public static final String COMMAND_WORD = "list";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": lists the tasks in the calendar";

    private static final int LIST_ALL = 1;
    private static final int LIST_BY_CATEGORY = 2;
    private static final int LIST_BY_DATE = 3;

    public ListCommand(String userCommand) {
        this.userInput = userCommand;
    }

    @Override
    public CommandResult execute() throws ProjException {

        String feedback = "";
        ArrayList<Integer> listTaskIndex = new ArrayList<>();

        String category = getCategory(userInput).trim().toUpperCase();
        String date = getDate(userInput).trim();
        String time = getTime(userInput).trim();

        int listCmdSubtype = getCmdSubtype(category, date, time);

        switch (listCmdSubtype) {
            case LIST_ALL:
                getWholeList(listTaskIndex);
                break;

            case LIST_BY_CATEGORY:
                getListByCategory(listTaskIndex, category);
                break;

            case LIST_BY_DATE:
                getListByDate(listTaskIndex, date, time);
                break;


            default:
                // Should not reach here
                feedback = "[Error][List] No such option to filter";
                break;
        }

        feedback = getFormattedFeedback(listTaskIndex);

        return new CommandResult(feedback);
    }

    // Shouldn't be called dummy

    private void getListByDate(ArrayList<Integer> listTaskIndex, String date, String time) {

        //only task can do it
        if (time == null || time.isEmpty()) {
            String[] dates = date.split("\\s+");
            //dates input dates
            HashSet<LocalDate> inputDates = new HashSet<>();
            for (String d : dates) {
                LocalDate addedDate = CalendarParser.convertToDate(d);
                if (addedDate.compareTo(LocalDate.now()) < 0) {
                    throw new NumberFormatException("Please enter a date that is either today or in the future.");
                }
                inputDates.add(addedDate);
            }

            int index = -1;
            int size = inputDates.size();
            for (Task task : taskList.getList()) {
                index++;
                if (task.getCategory().equals("CLASS"))
                    continue;
                ArrayList<LocalDate> localDates = task.getDate();
                int sum = 0;

                for (LocalDate d : localDates) {
                    if (inputDates.contains(d))
                        sum++;
                    if (sum >= size) {
                        listTaskIndex.add(index);
                        break;
                    }
                }
                // Populate the date with current date if date is not inputted
            }
            return;
        }


        if (date.isEmpty()) {
            String[] times = time.split("\\s+");
            ArrayList<LocalTime> startTimes = new ArrayList<>();
            ArrayList<LocalTime> endTimes = new ArrayList<>();
            for (String atime : times) {
                String[] timeRange = atime.split("-");
                LocalTime startTime = LocalTime.parse(timeRange[0], DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(timeRange[1], DateTimeFormatter.ofPattern("HH:mm"));
                startTimes.add(startTime);
                endTimes.add(endTime);
            }

            int size = startTimes.size();

            for (int i = 0; i < taskList.getListSize(); i++) {
                Task task = taskList.getTask(i);

                ArrayList<LocalTime> localTimes = task.getTime();

                int sum = 0;
                for (int j = 0; j < localTimes.size()/2; j++) {
                    for (int k = 0; k < size; k++) {
                        if (localTimes.get(2 * j).equals(startTimes.get(k))
                                && localTimes.get(2 * j + 1).equals(endTimes.get(k))) {
                            sum++;
                        }
                    }
                    if (sum >= size) {
                        listTaskIndex.add(i);
                        break;
                    }
                }
            }

        }

        //date and time
        if (!date.isEmpty() && !time.isEmpty()) {
            String[] dates = date.split("\\s+");
            String[] times = time.split("\\s+");
            ArrayList<LocalTime> startTimes = new ArrayList<>();
            ArrayList<LocalTime> endTimes = new ArrayList<>();
            ArrayList<LocalDate> dateList = new ArrayList<>();

            for (String atime : times) {
                String[] timeRange = atime.split("-");
                LocalTime startTime = LocalTime.parse(timeRange[0], DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(timeRange[1], DateTimeFormatter.ofPattern("HH:mm"));
                startTimes.add(startTime);
                endTimes.add(endTime);
            }

            for (String adate : dates) {
                dateList.add(LocalDate.parse(adate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            //dates input dates
            int size = dates.length;

            for (int i = 0; i < taskList.getListSize(); i++) {
                Task task = taskList.getTask(i);
                String category = task.getCategory().trim();
                if (category.equals("CLASS"))
                    continue;

                ArrayList<LocalTime> localTimes = task.getTime();
                ArrayList<LocalDate> localDates = task.getDate();

                int sum = 0;

                for (int j = 0; j < localDates.size(); j++) {
                    for (int k = 0; k < dateList.size(); k++) {
                        if (localTimes.get(2 * j).equals(startTimes.get(k))
                                && localTimes.get(2 * j + 1).equals(endTimes.get(k))
                                && localDates.get(j).equals(dateList.get(k))) {
                            sum++;
                        }
                    }

                    if (sum >= size) {
                        listTaskIndex.add(i);
                        break;
                    }
                }
            }
        }


    }


    private void getWholeList(ArrayList<Integer> listTaskIndex) {
        for (int i = 0; i < taskList.getListSize(); i++) {
            //System.out.println(taskList.getTask(i).toString());
            listTaskIndex.add(i);
        }
    }

    private String getFormattedFeedback(ArrayList<Integer> listTaskIndex) {

        String feedback;
        if (listTaskIndex.size() == 0 || listTaskIndex.size() == 1) {
            feedback = "There are " + listTaskIndex.size() + " task.\n";
        } else {
            feedback = "There are " + listTaskIndex.size() + " tasks.\n";
        }

        for (int i = 0; i < listTaskIndex.size(); i++) {
            Integer taskIndex = listTaskIndex.get(i);
            Task task = taskList.getTask(taskIndex);
            if (task instanceof TaskNonclass) {
                feedback += TAB + TAB + (i + TASKLIST_OFFSET) + ". ";
                feedback += "[" + ((TaskNonclass) task).getStatusIcon() + "] " + task + "\n";
            }
            if (task instanceof Class) {
                feedback += TAB + TAB + (i + TASKLIST_OFFSET) + ". ";
                feedback += "[" + ((Class) task).getStatusIcon() + "] " + task + "\n";
            }

        }

        return feedback;
    }

    private void getListByCategory(ArrayList<Integer> listTaskIndex, String category) throws ProjException {

        if (!taskList.containsCategory(category)) {
            ui.showAllCategory(taskList.getAllCategory());
            throw new ProjException(TAB + "There is no " + category + " in current category.\n"
                    + Ui.DIVIDER);
        }

        for (Integer taskIndex : taskList.getCategoryTask(category)) {
            listTaskIndex.add(taskIndex);
        }
    }

    private int getCmdSubtype(String category, String date, String time) {

        if (date.isEmpty() && !category.isEmpty()) {
            return LIST_BY_CATEGORY;
        }

        if (!(date.isEmpty() && time.isEmpty()) && category.isEmpty()) {
            return LIST_BY_DATE;
        }

        return LIST_ALL;
    }


}
