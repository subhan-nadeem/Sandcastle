package com.subhan_nadeem.sandcastle.models;

/**
 * Created by Subhan Nadeem on 2017-10-21.
 * Options passed to the Toolbar Controller when toolbars are changed
 * Utilizes builder pattern
 */

public class ToolbarOptions {
    private final String title;
    private final Integer margins;
    private final String subtitle;

    private ToolbarOptions(Builder builder) {
        this.title = builder.title;
        this.margins = builder.margins;
        this.subtitle = builder.subtitle;
    }

    public String getTitle() {
        return title;
    }

    public Integer getMargins() {
        return margins;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public static class Builder {
        private String title;
        private Integer margins;

        public Builder setSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        private String subtitle;

        public Builder setText(String text) {
            this.title = text;
            return this;
        }

        public Builder setMargins(Integer margins) {
            this.margins = margins;
            return this;
        }


        public ToolbarOptions build() {
            return new ToolbarOptions(this);
        }

    }
}