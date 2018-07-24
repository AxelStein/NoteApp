package com.axel_stein.noteapp;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.LabelCache;
import com.axel_stein.domain.model.NoteCache;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.model.NotebookCache;

import org.greenrobot.eventbus.EventBus;

public class EventBusHelper {

    public static void subscribe(@NonNull Object o) {
        EventBus.getDefault().register(o);
    }

    public static void unsubscribe(@NonNull Object o) {
        EventBus.getDefault().unregister(o);
    }

    public static void updateNoteList() {
        updateNoteList(true, false);
    }

    public static void updateDrawer() {
        post(new UpdateDrawer(true, false));
    }

    public static void updateNoteList(boolean saveSelection, boolean click) {
        NoteCache.invalidate();
        NotebookCache.invalidate();
        LabelCache.invalidate();
        post(new UpdateNoteList());
        post(new UpdateDrawer(saveSelection, click));
    }

    public static void addNotebook(Notebook notebook) {
        NotebookCache.invalidate();
        post(new AddNotebook(notebook));
    }

    public static void renameNotebook(Notebook notebook) {
        NotebookCache.invalidate();
        post(new RenameNotebook(notebook));
    }

    public static void deleteNotebook(Notebook notebook) {
        NotebookCache.invalidate();
        post(new DeleteNotebook(notebook));
    }

    public static void addLabel(Label label) {
        LabelCache.invalidate();
        post(new AddLabel(label));
    }

    public static void renameLabel(Label label) {
        LabelCache.invalidate();
        post(new RenameLabel(label));
    }

    public static void deleteLabel(Label label) {
        LabelCache.invalidate();
        post(new DeleteLabel(label));
    }

    public static void showMessage(String string) {
        post(new Message(string));
    }

    public static void showMessage(int stringRes) {
        post(new Message(stringRes));
    }

    public static void showMessage(int stringRes, int actionNameRes, Runnable action) {
        post(new Message(stringRes, actionNameRes, action));
    }

    public static void recreate() {
        post(new Recreate());
    }

    public static void updateAddNoteFAB() {
        post(new UpdateAddNoteFAB());
    }

    private static void post(@NonNull Object o) {
        EventBus.getDefault().postSticky(o);
    }

    public static class UpdateAddNoteFAB {
    }

    public static class Recreate {
    }

    public static class UpdateDrawer {
        private boolean saveSelection;
        private boolean click;

        UpdateDrawer(boolean saveSelection, boolean click) {
            this.saveSelection = saveSelection;
            this.click = click;
        }

        public boolean saveSelection() {
            return saveSelection;
        }

        public boolean click() {
            return click;
        }
    }

    public static class UpdateNoteList {
    }

    public static class AddNotebook {
        private Notebook notebook;

        AddNotebook(Notebook notebook) {
            this.notebook = notebook;
        }

        public Notebook getNotebook() {
            return notebook;
        }
    }

    public static class RenameNotebook {
        private Notebook notebook;

        RenameNotebook(Notebook notebook) {
            this.notebook = notebook;
        }

        public Notebook getNotebook() {
            return notebook;
        }
    }

    public static class DeleteNotebook {
        private Notebook notebook;

        DeleteNotebook(Notebook notebook) {
            this.notebook = notebook;
        }

        public Notebook getNotebook() {
            return notebook;
        }
    }

    public static class AddLabel {
        private Label label;

        AddLabel(Label label) {
            this.label = label;
        }

        public Label getLabel() {
            return label;
        }
    }

    public static class RenameLabel {
        private Label label;

        RenameLabel(Label label) {
            this.label = label;
        }

        public Label getLabel() {
            return label;
        }
    }

    public static class DeleteLabel {
        private Label label;

        DeleteLabel(Label label) {
            this.label = label;
        }

        public Label getLabel() {
            return label;
        }
    }

    public static class Message {
        private String msg;
        private int msgRes;
        private Runnable action;
        private int actionName;

        public Message(String msg) {
            this.msg = msg;
        }

        Message(int msgRes) {
            this.msgRes = msgRes;
        }

        Message(int msgRes, int actionName, Runnable action) {
            this.msgRes = msgRes;
            this.actionName = actionName;
            this.action = action;
        }

        public boolean hasMsgRes() {
            return msgRes > 0;
        }

        public String getMsg() {
            return msg;
        }

        public int getMsgRes() {
            return msgRes;
        }

        public Runnable getAction() {
            return action;
        }

        public boolean hasAction() {
            return action != null;
        }

        public int getActionName() {
            return actionName;
        }

        public boolean hasActionNameRes() {
            return actionName > 0;
        }

    }

}

