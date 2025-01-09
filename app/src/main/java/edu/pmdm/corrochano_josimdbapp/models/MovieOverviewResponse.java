package edu.pmdm.corrochano_josimdbapp.models;

import java.util.List;

public class MovieOverviewResponse {
    private Data data;
    private boolean status;
    private String message;

    // Getters and setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Data {
        private Title title;

        // Getters and setters
        public Title getTitle() {
            return title;
        }

        public void setTitle(Title title) {
            this.title = title;
        }
    }

    public static class Title {
        private String id;
        private TitleText titleText;
        private TitleText originalTitleText;
        private ReleaseYear releaseYear;
        private ReleaseDate releaseDate;
        private TitleType titleType;
        private PrimaryImage primaryImage;
        private RatingsSummary ratingsSummary;
        private EngagementStatistics engagementStatistics;
        private Plot plot;
        private Certificate certificate;
        private Runtime runtime;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public TitleText getTitleText() {
            return titleText;
        }

        public void setTitleText(TitleText titleText) {
            this.titleText = titleText;
        }

        public TitleText getOriginalTitleText() {
            return originalTitleText;
        }

        public void setOriginalTitleText(TitleText originalTitleText) {
            this.originalTitleText = originalTitleText;
        }

        public ReleaseYear getReleaseYear() {
            return releaseYear;
        }

        public void setReleaseYear(ReleaseYear releaseYear) {
            this.releaseYear = releaseYear;
        }

        public ReleaseDate getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(ReleaseDate releaseDate) {
            this.releaseDate = releaseDate;
        }

        public TitleType getTitleType() {
            return titleType;
        }

        public void setTitleType(TitleType titleType) {
            this.titleType = titleType;
        }

        public PrimaryImage getPrimaryImage() {
            return primaryImage;
        }

        public void setPrimaryImage(PrimaryImage primaryImage) {
            this.primaryImage = primaryImage;
        }

        public RatingsSummary getRatingsSummary() {
            return ratingsSummary;
        }

        public void setRatingsSummary(RatingsSummary ratingsSummary) {
            this.ratingsSummary = ratingsSummary;
        }

        public EngagementStatistics getEngagementStatistics() {
            return engagementStatistics;
        }

        public void setEngagementStatistics(EngagementStatistics engagementStatistics) {
            this.engagementStatistics = engagementStatistics;
        }

        public Plot getPlot() {
            return plot;
        }

        public void setPlot(Plot plot) {
            this.plot = plot;
        }

        public Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(Certificate certificate) {
            this.certificate = certificate;
        }

        public Runtime getRuntime() {
            return runtime;
        }

        public void setRuntime(Runtime runtime) {
            this.runtime = runtime;
        }
    }

    public static class TitleText {
        private String text;

        // Getters and setters
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class ReleaseYear {
        private int year;
        private Integer endYear;

        // Getters and setters
        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public Integer getEndYear() {
            return endYear;
        }

        public void setEndYear(Integer endYear) {
            this.endYear = endYear;
        }
    }

    public static class ReleaseDate {
        private int month;
        private int day;
        private int year;

        // Getters and setters
        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }
    }

    public static class TitleType {
        private String text;

        // Getters and setters
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class PrimaryImage {
        private String url;

        // Getters and setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class RatingsSummary {
        private double aggregateRating;
        private int voteCount;

        // Getters and setters
        public double getAggregateRating() {
            return aggregateRating;
        }

        public void setAggregateRating(double aggregateRating) {
            this.aggregateRating = aggregateRating;
        }

        public int getVoteCount() {
            return voteCount;
        }

        public void setVoteCount(int voteCount) {
            this.voteCount = voteCount;
        }
    }

    public static class EngagementStatistics {
        private WatchlistStatistics watchlistStatistics;

        // Getters and setters
        public WatchlistStatistics getWatchlistStatistics() {
            return watchlistStatistics;
        }

        public void setWatchlistStatistics(WatchlistStatistics watchlistStatistics) {
            this.watchlistStatistics = watchlistStatistics;
        }

        public static class WatchlistStatistics {
            private DisplayableCount displayableCount;

            // Getters and setters
            public DisplayableCount getDisplayableCount() {
                return displayableCount;
            }

            public void setDisplayableCount(DisplayableCount displayableCount) {
                this.displayableCount = displayableCount;
            }

            public static class DisplayableCount {
                private String text;

                // Getters and setters
                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }
            }
        }
    }

    public static class Plot {
        private PlotText plotText;

        // Getters and setters
        public PlotText getPlotText() {
            return plotText;
        }

        public void setPlotText(PlotText plotText) {
            this.plotText = plotText;
        }

        public static class PlotText {
            private String plainText;

            // Getters and setters
            public String getPlainText() {
                return plainText;
            }

            public void setPlainText(String plainText) {
                this.plainText = plainText;
            }
        }
    }

    public static class Certificate {
        private String rating;

        // Getters and setters
        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }
    }

    public static class Runtime {
        private int seconds;

        // Getters and setters
        public int getSeconds() {
            return seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }
    }
}
