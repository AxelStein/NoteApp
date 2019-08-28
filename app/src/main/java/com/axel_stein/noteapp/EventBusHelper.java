package com.axel_stein.noteapp;

import androidx.annotation.NonNull;

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

    public static void signOut() {
        post(new SignOutEvent());
    }

    public static void updateNoteList() {
        NoteCache.invalidate();
        NotebookCache.invalidate();
        post(new UpdateNoteList());
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

    public static void showMessage(String string) {
        post(new Message(string));
    }

    public static void showMessage(int stringRes) {
        post(new Message(stringRes));
    }

    public static void showMessage(int stringRes, int delay) {
        post(new Message(stringRes, delay));
    }

    public static void showMessage(int stringRes, int actionNameRes, Runnable action) {
        post(new Message(stringRes, actionNameRes, action));
    }

    public static void recreate() {
        post(new Recreate());
    }

    private static void post(@NonNull Object o) {
        EventBus.getDefault().postSticky(o);
    }

    public static class SignOutEvent {
    }

    public static class Recreate {
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

    public static class Message {
        private String msg;
        private int msgRes;
        private Runnable action;
        private int actionName;
        private int delay;

        public Message(String msg) {
            this.msg = msg;
        }

        Message(int msgRes) {
            this.msgRes = msgRes;
        }

        Message(int msgRes, int delay) {
            this.msgRes = msgRes;
            this.delay = delay;
            if (delay < 0) {
                this.delay = 0;
            }
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

        public int getDelay() {
            return delay;
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

