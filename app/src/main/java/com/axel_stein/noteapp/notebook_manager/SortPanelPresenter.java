package com.axel_stein.noteapp.notebook_manager;

public class SortPanelPresenter {

    public interface PanelView {

        void setVisible(boolean visible);

        void setItemCount(String text);

        void setSortTitle(String title, boolean enableIndicator, boolean desc);

        void onClick();
    }

    public interface OnSortTitleClickListener {
        void onSortTitleClick();
    }

    public interface ItemListView {
        void updateOrder(boolean desc);
    }

    private PanelView mPanelView;

    private boolean desc;

    public void setItemCount(String text) {
        mPanelView.setItemCount(text);
    }

    public void setCurrentSortTitle(String text, boolean desc) {

    }

    public void toggleSortOrder() {
        this.desc = !desc;
    }

}
