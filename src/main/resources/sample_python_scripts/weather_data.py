import random
import time
import json

def generate_weather_data():
    cities = ["New York", "London", "Tokyo", "Sydney", "Moscow"]

    while True:
        weather_data = []
        for city in cities:
            temperature = round(random.uniform(-10, 40), 1)
            humidity = random.randint(0, 100)
            wind_speed = round(random.uniform(0, 30), 1)
            conditions = random.choice(["Sunny", "Cloudy", "Rainy", "Snowy"])

            weather_data.append({
                "city": city,
                "temperature": temperature,
                "humidity": humidity,
                "wind_speed": wind_speed,
                "conditions": conditions
            })

        timestamp = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        output = {
            "timestamp": timestamp,
            "weather_data": weather_data
        }
        print(json.dumps(output))
        time.sleep(10)

if __name__ == "__main__":
    generate_weather_data()