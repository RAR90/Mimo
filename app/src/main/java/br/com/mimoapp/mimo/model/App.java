package br.com.mimoapp.mimo.model;

/**
 * Created by rafael on 12/12/17.
 */

public class App {

    private String priceUnits;
    private String priceCents;
    private String aboutTitle;
    private String aboutText;

    public App() {

    }

    public String getPriceUnits() {
        return priceUnits;
    }

    public void setPriceUnits(String priceUnits) {
        this.priceUnits = priceUnits;
    }

    public String getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(String priceCents) {
        this.priceCents = priceCents;
    }

    public String getAboutTitle() {
        return aboutTitle;
    }

    public void setAboutTitle(String aboutTitle) {
        this.aboutTitle = aboutTitle;
    }

    public String getAboutText() {
        return aboutText;
    }

    public void setAboutText(String aboutText) {
        this.aboutText = aboutText;
    }
}
