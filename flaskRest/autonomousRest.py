import threading
import time
from flask import Flask, send_file, make_response, request
import io
import json
import autonomous.car_controller
from PIL import Image

app = Flask(__name__)

car = autonomous.car_controller.CarController('autonomous-platform.local')

"""comment out everthing with car if you want to try the server on local without a connection to the car. And return 
a string instead Example: return make_response(speed) when connected to car return "something"
for local test in get_speed"""

lock = threading.Lock()
buff = io.BytesIO()


@app.before_first_request
def activate_background_work():
    def run_job():
        while True:
            pic = car.get_picture(0)
            newImage = pic.resize((300, 200))
            newImage.save(buff, "jpeg", quality=30, optimize=True)
            with lock:
                buff.seek(0)
            time.sleep(0.005)

    thread = threading.Thread(target=run_job)
    thread.start()

# root
@app.route("/")
def index():
    return "This is Root!"


@app.route('/image', methods=['GET'])
def get_image():
    with lock:
        data = buff.getvalue()
    fileSend = send_file(io.BytesIO(data), mimetype="image/jpeg", attachment_filename="pic.jpeg")
    return make_response(fileSend)


@app.route('/location', methods=['GET'])
def get_location():
    """todo get real car location"""
    lat = 57.704947
    lng = 11.963594

    location = [
        {'lat': lat, 'lng': lng}
    ]

    return json.dumps(location)


@app.route('/battery', methods=['GET'])
def get_battery():
    """currentBattery = (car.get_voltage())
    return make_response(str(currentBattery))"""
    return make_response("16800")


@app.route('/speed', methods=['GET'])
def get_speed():
    """wheel1 = car.get_wheel_speeds()[0] / 100
    wheel2 = car.get_wheel_speeds()[1] / 100
    wheel3 = car.get_wheel_speeds()[2] / 100
    wheel4 = car.get_wheel_speeds()[3] / 100
    allWheels = (wheel1 + wheel2 + wheel3 + wheel4) / 4
    speedDecimal = str(allWheels)
    speed = speedDecimal[:3]
    return make_response(speed)"""
    return make_response("0")


@app.route('/steer', methods=['POST'])
def set_steering():
    print(request.form['turn'])
    print(request.form['speed'])
    turn = request.form['turn']
    speed = request.form['speed']
    car.set_turnrate(-int(float(turn)))
    car.set_speed(int(float(speed)))

    return make_response("ok")


@app.route('/lock', methods=['POST'])
def set_lock():
    lock = request.form['lock']

    if lock == "false":
        print(str(lock))
        lock = False
        car.arm_motors()
    else:
        print(str(lock))
        lock = True
        car.disarm_motors()
    return "ok"


@app.route('/motorStop', methods=['POST'])
def get_motor():
    lock = request.form['lock']
    print(str(lock))

    car.disarm_motors()
    return "ok"


if __name__ == '__main__':
    app.run(host='0.0.0.0', threaded=True, port=5000)
