import random
import time
import json

def generate_stock_data():
    stocks = {
        "AAPL": 150.0,
        "GOOGL": 2800.0,
        "AMZN": 3300.0,
        "MSFT": 300.0,
        "TSLA": 700.0
    }

    while True:
        for stock, price in stocks.items():
            change = random.uniform(-5, 5)
            new_price = max(0, price + change)
            stocks[stock] = round(new_price, 2)

        timestamp = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        output = {
            "timestamp": timestamp,
            "stocks": stocks
        }
        print(json.dumps(output))
        time.sleep(5)

if __name__ == "__main__":
    generate_stock_data()