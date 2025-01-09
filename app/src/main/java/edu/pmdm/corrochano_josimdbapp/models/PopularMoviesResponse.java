package edu.pmdm.corrochano_josimdbapp.models;

import java.util.List;

public class PopularMoviesResponse {

    private Data data;
    private boolean status;
    private String message;

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

        private TopMeterTitles topMeterTitles;

        public TopMeterTitles getTopMeterTitles() {
            return topMeterTitles;
        }

        public void setTopMeterTitles(TopMeterTitles topMeterTitles) {
            this.topMeterTitles = topMeterTitles;
        }

        public static class TopMeterTitles {
            private List<Edge> edges;

            public List<Edge> getEdges() {
                return edges;
            }

            public void setEdges(List<Edge> edges) {
                this.edges = edges;
            }
        }
    }

    public static class Edge {

        private Node node;

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

    }

    public static class Node {

        private String id;
        private TitleText titleText;
        private PrimaryImage primaryImage;

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

        public PrimaryImage getPrimaryImage() {
            return primaryImage;
        }

        public void setPrimaryImage(PrimaryImage primaryImage) {
            this.primaryImage = primaryImage;
        }
    }

    public static class TitleText {

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

    }

    public static class PrimaryImage {

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }
}
