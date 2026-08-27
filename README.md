# AI Market Analyst

A system demonstrating AI implementation on real-time financial market data: fetching live stock and crypto prices, running automated AI trend analysis, and tracking prediction accuracy over time.

**Disclaimer:** This is an educational/analytical tool only. It does not constitute investment advice.

## What it does
1. Fetches real-time price data for 5 assets (NVDA, AAPL, TSLA, SPY, SOL/USD) via the Twelve Data API
2. Sends the data to the Gemini API for trend analysis (Bullish/Bearish/Neutral)
3. Compares yesterday's prediction against today's actual result, logging it to a cumulative accuracy tracker
4. Generates an HTML report with a data table and an accuracy chart

## How it works
- Java 21, Maven
- Twelve Data API for prices, Gemini API for analysis
- API keys stored in `.env` (excluded from Git)
- Accuracy log stored in JSON Lines format, appended on every run

## Running it
1. Add a `.env` file with `TWELVE_DATA_API_KEY` and `GEMINI_API_KEY`
2. Run `Main.java`
3. Open `report.html` in a browser