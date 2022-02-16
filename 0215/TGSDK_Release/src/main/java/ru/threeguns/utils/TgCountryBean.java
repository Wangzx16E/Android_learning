package ru.threeguns.utils;

import java.util.List;

public class TgCountryBean  {


    private List<CountryBean> country;

    public List<CountryBean> getCountry() {
        return country;
    }

    public void setCountry(List<CountryBean> country) {
        this.country = country;
    }

    public static class CountryBean {
        /**
         * chineseName : 安哥拉
         * country : 安哥拉
         * region : 安哥拉
         * shortName : 安哥拉
         * showName : 安哥拉
         */

        private String chineseName;
        private String country;
        private String region;
        private String shortName;
        private String showName;

        public String getChineseName() {
            return chineseName;
        }

        public void setChineseName(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }
    }
}
