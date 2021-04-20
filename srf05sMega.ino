#include <ros.h>
//#include <std_msgs/MultiArrayLayout.h>
//#include <std_msgs/MultiArrayDimension.h>
#include <std_msgs/Float32MultiArray.h>

const unsigned int N = 6;
const unsigned int TRIG_PIN[N] = {30,32,34,38,40,42};
const unsigned int ECHO_PIN[N] = {31,33,35,39,41,43};
const unsigned int BAUD_RATE=9600;
unsigned long duration;
float distance[N];
ros::NodeHandle  nh;
std_msgs::Float32MultiArray dist_msg;
ros::Publisher pub("distList", &dist_msg);

void setup() {
  for (int s=0; s<N; s++){
    pinMode(TRIG_PIN[s], OUTPUT);
    pinMode(ECHO_PIN[s], INPUT);
  }
  nh.getHardware()->setBaud(BAUD_RATE);
  nh.initNode();
  dist_msg.layout.dim = (std_msgs::MultiArrayDimension *)
  malloc(sizeof(std_msgs::MultiArrayDimension)*2);
  dist_msg.layout.dim[0].label = "height";
  dist_msg.layout.dim[0].size = N;
  dist_msg.layout.dim[0].stride = 1;
  dist_msg.layout.data_offset = 0;
  dist_msg.data = (float *)malloc(sizeof(float)*8);
  dist_msg.data_length = N;
  nh.advertise(pub);
  Serial.begin(BAUD_RATE);
}

void loop() {
  //dist_msg.data.clear();
  for (int s=0; s<N; s++){
    digitalWrite(TRIG_PIN[s], LOW);
    delayMicroseconds(2);
    digitalWrite(TRIG_PIN[s], HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIG_PIN[s], LOW);
    duration = pulseIn(ECHO_PIN[s], HIGH);
    distance[s] = (float)duration/29/2;
    if(duration==0){
      Serial.print("第 ");Serial.print(s+1);
      Serial.println(" 个測距器无脈衝！");
    } else {
      Serial.print(s+1);Serial.print(" 號距離最近物體：");
      Serial.print(distance[s]);
      Serial.println(" cm");
    }
    dist_msg.data[s] = distance[s];
  }
  //dist_msg.data = distance;
  pub.publish(&dist_msg);
  nh.spinOnce();
  delay(200);
}
