package com.example.videoplayer.MediaPlayerClasses;

public class IconModel {
    private int imageView;
    private String iconTitle;
    IconModel(int imageView, String iconTitle)
    {
        this.imageView=imageView;
        this.iconTitle=iconTitle;
    }

    public int getImageView() {
        return imageView;
    }

    public void setImageView(int imageView) {
        this.imageView = imageView;
    }

    public String getIconTitle() {
        return iconTitle;
    }

    public void setIconTitle(String iconTitle) {
        this.iconTitle = iconTitle;
    }
}
