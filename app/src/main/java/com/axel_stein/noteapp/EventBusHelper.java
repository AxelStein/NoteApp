package com.axel_stein.noteapp;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Notebook;

import org.greenrobot.eventbus.EventBus;

public class EventBusHelper {

    public static void subscribe(@NonNull Object o) {
        EventBus.getDefault().register(o);
    }

    public static void unsubscribe(@NonNull Object o) {
        EventBus.getDefault().unregister(o);
    }

    public static void updateNoteList() {
        post(new UpdateNoteList());
        post(new UpdateDrawer());
    }

    public static void addNotebook(Notebook notebook) {
        post(new AddNotebook(notebook));
    }

    public static void renameNotebook(Notebook notebook) {
        post(new RenameNotebook(notebook));
    }

    public static void deleteNotebook(Notebook notebook) {
        post(new DeleteNotebook(notebook));
    }

    public static void addLabel(Label label) {
        post(new AddLabel(label));
    }

    public static void renameLabel(Label label) {
        post(new RenameLabel(label));
    }

    public static void deleteLabel(Label label) {
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

    public static void updateDrawer() {
        post(new UpdateDrawer());
    }

    private static void post(@NonNull Object o) {
        EventBus.getDefault().postSticky(o);
    }

    public static class Recreate {
    }

    public static class UpdateDrawer {
    }

    public static class UpdateNoteList {
    }

    public static class AddNotebook {
        private Notebook notebook;

        public AddNotebook(Notebook notebook) {
            this.notebook = notebook;
        }

        public Notebook getNotebook() {
            return notebook;
        }
    }

    public static class RenameNotebook {
        private Notebook notebook;

        public RenameNotebook(Notebook notebook) {
            this.notebook = notebook;
        }

        public Notebook getNotebook() {
            return notebook;
        }
    }

    public static class DeleteNotebook {
        private Notebook notebook;

        public DeleteNotebook(Notebook notebook) {
            this.notebook = notebook;
        }

        public Notebook getNotebook() {
            return notebook;
        }
    }

    public static class AddLabel {
        private Label label;

        public AddLabel(Label label) {
            this.label = label;
        }

        public Label getLabel() {
            return label;
        }
    }

    public static class RenameLabel {
        private Label label;

        public RenameLabel(Label label) {
            this.label = label;
        }

        public Label getLabel() {
            return label;
        }
    }

    public static class DeleteLabel {
        private Label label;

        public DeleteLabel(Label label) {
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
        private String actionName;
        private int actionNameRes;

        public Message(String msg) {
            this.msg = msg;
        }

        public Message(int msgRes) {
            this.msgRes = msgRes;
        }

        public Message(String msg, String actionName, Runnable action) {
            this.msg = msg;
            this.actionName = actionName;
            this.action = action;
        }

        public Message(int msgRes, int actionNameRes, Runnable action) {
            this.msgRes = msgRes;
            this.actionNameRes = actionNameRes;
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

        public String getActionName() {
            return actionName;
        }

        public int getActionNameRes() {
            return actionNameRes;
        }

        public boolean hasActionNameRes() {
            return actionNameRes > 0;
        }

    }

}

