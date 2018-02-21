from flask import Flask, send_file, make_response, Response, request
import io
import autonomous.car_controller

app = Flask(__name__)

car = autonomous.car_controller.CarController('autonomous-platform.local')

"""comment out everthing with car if you want to try the server on local without a connection to the car. And return a string instead"""
""" Example: return make_response(speed) when connected to car
    return "something" for local test in get_speed"""


# root
@app.route("/")
def index():
    return "This is Root!"


# GET
@app.route('/image', methods=['GET'])
def get_image():
    buff = io.BytesIO()
    pic = car.get_picture(0)
    pic.save(buff, "jpeg")
    buff.seek(0)
    fileSend = send_file(buff, mimetype="image/jpeg", attachment_filename="pic.jpeg")
    return make_response(fileSend)


@app.route('/speed', methods=['GET'])
def get_speed():
    wheel1 = car.get_wheel_speeds()[0] / 100
    wheel2 = car.get_wheel_speeds()[1] / 100
    wheel3 = car.get_wheel_speeds()[2] / 100
    wheel4 = car.get_wheel_speeds()[3] / 100
    allWheels = (wheel1 + wheel2 + wheel3 + wheel4) / 4
    speedDecimal = str(allWheels)
    speed = speedDecimal[:3]

    return make_response(speed)


"""return make_response(speed) when connected to car
    return "something" for local test """


@app.route('/lock', methods=['POST'])
def set_lock():
    lock = request.form['lock']

    if lock:
        print(str(lock))
        lock = False
        """car.arm_motors()"""
    else:
        print(str(lock))
        lock = False
        """car.disarm_motors()"""
    return "ok"


@app.route('/steer', methods=['POST'])
def set_steering():
    print(request.form['turn'])
    print(request.form['speed'])
    turn = request.form['turn']
    speed = request.form['speed']
    """car.set_turnrate(int(turn))
    car.set_speed(int(speed))
    """
    return make_response("ok")


@app.route('/motorStop', methods=['post'])
def get_motor():
    lock = request.form['lock']
    print(str(lock))


    """car.disarm_motors()"""
    return "ok"


if __name__ == '__main__':
    app.run(host='0.0.0.0', threaded=True, port=5000)
    """threaded=true for more clients"""

"""
    while True:
        test = 'heeej'.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(5))
        return make_response(test)
        """
