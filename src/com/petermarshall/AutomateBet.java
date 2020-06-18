package com.petermarshall;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AutomateBet {
    private static final String UNIBET_LINK = "https://www.unibet.co.uk/betting/sports/filter/football";

    public static void main(String[] args) {
        placeBet("Slovenia", "Prva Liga", "NK Triglav Kranj", "Maribor", 2, 0.1, 1.3);
    }

    //int result 0 = home, 1 = draw, 2 = away
    public static BetPlacedUniBet placeBet(String targetCountry, String leagueName, String homeTeam, String awayTeam, int result, double amount, double minOdds) {
        int stake = 5;
        BetPlacedUniBet bet = new BetPlacedUniBet(-1,stake,false);
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 30);
        try {
            driver.get(UNIBET_LINK);
            //close annoying cover page showing offers. enclosed in try because sometimes does not appear
            try {
                wait.until(presenceOfElementLocated(By.cssSelector("button[type='reset']")));
                WebElement closeBtn = driver.findElement(By.cssSelector("button[type='reset']"));
                Thread.sleep(2010); //site makes link unclickable for some time
                closeBtn.click();
            } catch (Exception e) {
                System.out.println("No annoying cover page. Continuing as normal");
            }
            //login
            wait.until(presenceOfElementLocated(By.cssSelector("input[data-test-name='field-username']"))).sendKeys(PrivateKeys.USERNAME);
            driver.findElement(By.cssSelector("input[data-test-name='field-password']")).sendKeys(PrivateKeys.PASSWORD);
            WebElement loginBtn = wait.until(presenceOfElementLocated(By.cssSelector("button[data-test-name='btn-login']")));
            loginBtn.click();
            Thread.sleep(5000); //waiting for the page to reload otherwise we will find containers before page reload.
            //wait for market info
            Thread.sleep(20000); //extra long wait as webpage can take a really long time to load in countries
            wait.until(presenceOfElementLocated(By.cssSelector(".KambiBC-collapsible-container")));
            Thread.sleep(5000); //letting the webpage load in all countries
            ArrayList<WebElement> allCountries = (ArrayList<WebElement>) driver.findElements(By.cssSelector(".KambiBC-mod-event-group-container"));
            //get rid of annoying cookies notice. should be done here as cookie overlay can block click events on the odds.
            wait.until(presenceOfElementLocated(By.cssSelector("#CybotCookiebotDialogBodyButtonAccept"))).click();
            //look for correct league
            countryLoop:
            for (WebElement country: allCountries) {
                String uniBetCountry = country.findElement(By.cssSelector("header span")).getText().trim(); //title should be first span in group
                if (uniBetCountry.equals(targetCountry)) {
                    if (!country.getAttribute("class").contains("KambiBC-expanded")) {
                        country.click();
                        Thread.sleep(1500); //allowing webpage to load in leagues and games of country
                    }
                    ArrayList<WebElement> leaguesInCountry = (ArrayList<WebElement>) country.findElements(By.cssSelector(".KambiBC-betoffer-labels__title"));
                    ArrayList<WebElement> gamesInLeagues = (ArrayList<WebElement>) country.findElements(By.cssSelector(".KambiBC-list-view__event-list"));
                    for (int i = 0; i<leaguesInCountry.size(); i++) {
                        String league = leaguesInCountry.get(i).getText().trim();
                        if (league.equals(leagueName)) {
                            ArrayList<WebElement> games = (ArrayList<WebElement>) gamesInLeagues.get(i).findElements(By.cssSelector(".KambiBC-event-item__event-wrapper"));
                            for (int g = 0; g < games.size(); g++) {
                                WebElement game = games.get(g);
                                ArrayList<WebElement> teamNames = (ArrayList<WebElement>) game.findElements(By.cssSelector(".KambiBC-event-participants__name"));
                                if (teamNames.get(0).getText().equals(homeTeam) || teamNames.get(1).getText().equals(awayTeam)) {
                                    ArrayList<WebElement> odds = (ArrayList<WebElement>) game.findElements(By.cssSelector("div[class*='onecrosstwo'] button"));
                                    WebElement relevantOdds = odds.get(result);
                                    String oddsStr = relevantOdds.findElements(By.cssSelector("div > div > div")).get(2).getText();
                                    double oddsOffered = Double.parseDouble(oddsStr);
                                    bet.setOddsOffered(oddsOffered);
                                    if (oddsOffered >= minOdds) {
                                        relevantOdds.click();
                                        break countryLoop;
                                    } else {
                                        return bet;
                                    }
                                }
                                //scroll latest game into view so next game can be seen
                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", game);
                                Thread.sleep(200);
                            }
                        }
                    }
                }
                //scrolling latest country into view so the next element is not covered by the header
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", country);
                Thread.sleep(200);
            };

            //filling out bet form and placing bet
            wait.until(presenceOfElementLocated(By.cssSelector(".mod-KambiBC-stake-input"))).sendKeys(amount+"");
            driver.findElement(By.cssSelector(".mod-KambiBC-betslip__place-bet-btn")).click();
            wait.until(presenceOfElementLocated(By.cssSelector(".mod-KambiBC-betslip-receipt__close-button"))).click();
            bet.setBetSuccessful(true);

            //logging out
            WebElement accountBtn = driver.findElement(By.cssSelector(".account-box-button"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountBtn);
            accountBtn.click();
            Thread.sleep(1000);
            wait.until(presenceOfElementLocated(By.cssSelector(".logout-link"))).click();
            Thread.sleep(5000);
        } catch(Exception e) {
            System.out.println(e);
        } finally {
            driver.quit();
        }
        return bet;
    }
}
