#define sensor1 A0//앞 적외선센서 핀 
#define sensor3 A2//cds
#include <Servo.h>
#include <L298Drv.h>


int distance1;

int servoPin1 = 9;     //오른팔 
int servoPin3 = 11;  

Servo servo1;      //함수 생성 
Servo servo3;      //함수 생성 

L298Drv motor1(7, 22);//오른쪽 바퀴
L298Drv motor2(8, 24);//왼쪽 바퀴

void Motor_Forward()//직진함수
{
  motor1.drive(50);//오른쪽 
  motor2.drive(50);//왼쪽
  Serial.println("Motor_Forward");
  
}

void Motor_Back()//뒤로
{
  motor1.drive(-10);
  motor2.drive(-10);
  Serial.println("Motor_back");
}

void Motor_leftTurn()
{
  motor1.drive(-30);
  motor2.drive(30);
}

void Motor_rightTurn()
{
  motor1.drive(30);
  motor2.drive(-30);
}

void Motor_Stop()
{
  motor1.drive(0);
  motor2.drive(0);
}

void s(){
  
float volts1 = analogRead(sensor1)*0.0048828125;
  distance1 = 13*pow(volts1,-1);
  if(distance1 <= 50){
    Serial.print("distance1: ");
    Serial.println(distance1);
  }
  delay(15);
}

 int q=0;
int lightSensor;
void cdss(){
  lightSensor = analogRead(sensor3);       //A2값을 읽어서 lightSensor에 넣는다.
  Serial.println(lightSensor);
  delay(100);
  if(lightSensor >200){
    q = 0;
  }
}

  int angle = 0;


void setup() {
Serial.begin(9600);
servo1.attach(servoPin1);
servo1.write(100);
servo3.attach(servoPin3);
servo3.write(30);
}
/*for(int a = 0;a<=3 ;a++){
                //scan from 0 to 180 degrees
                for(angle = 100; angle>30;angle-=3)
                {
                  servo1.write(angle);
                  delay(30);
                }
                //now scan back from 180 to 0 degrees
                for(angle = 30; angle<100;angle+=3)
                {
                  servo1.write(angle); 
                  delay(30);
                }
                servo1.write(100);
                delay(15);
              }

              delay(3000);

for(int a = 0;a<=3 ;a++){
                //scan from 0 to 180 degrees
                for(angle = 100; angle>30;angle-=3)
                {
                  servo2.write(angle);
                  delay(30);
                }
                //now scan back from 180 to 0 degrees
                for(angle = 30; angle<100;angle+=3)
                {
                  servo2.write(angle); 
                  delay(30);
                }
                servo2.write(100);
                delay(15);
              }
              양 송 만미리 쎄컨드
              나비보벳따우 만오천구백십 세컨드
                servo3.write(100);
    delay(3000);
    

for(int a = 0;a<=3 ;a++){
                //scan from 0 to 180 degrees
                Motor_leftTurn();
                delay(30);
                for(angle = 100; angle>30;angle-=3)
                {
                  servo1.write(angle);
                  servo3.write(angle);
                  delay(30);
                }
                //now scan back from 180 to 0 degrees
                Motor_rightTurn();
                delay(30);
                for(angle = 30; angle<100;angle+=3)
                {
                  servo1.write(angle);
                  servo3.write(angle);
                  delay(30);
                }
              }
              Serial.print(" 1 ");
              servo1.write(100);
              servo3.write(30);
              delay(10000);
               
              
    
              Motor_Stop();
              delay(10000);  servo3.write(100);
    delay(3000);
    

for(int a = 0;a<=3 ;a++){
                //scan from 0 to 180 degrees
                Motor_leftTurn();
                delay(30);
                for(angle = 100; angle>30;angle-=3)
                {
                  servo1.write(angle);
                  servo3.write(angle);
                  delay(30);
                }
                //now scan back from 180 to 0 degrees
                Motor_rightTurn();
                delay(30);
                for(angle = 30; angle<100;angle+=3)
                {
                  servo1.write(angle);
                  servo3.write(angle);
                  delay(30);
                }
              }
              Serial.print(" 1 ");
              servo1.write(100);
              servo3.write(30);
              delay(10000);
               
              
    
              Motor_Stop();
              delay(10000);
              */
              


           

void loop() {
 for(int a = 0;a<=3 ;a++){
                //scan from 0 to 180 degrees
                Motor_leftTurn();
                delay(30);
                for(angle = 100; angle>30;angle-=3)
                {
                  servo1.write(angle);
                  servo3.write(angle);
                  delay(30);
                }
                //now scan back from 180 to 0 degrees
                Motor_rightTurn();
                delay(30);
                for(angle = 30; angle<100;angle+=3)
                {
                  servo1.write(angle);
                  servo3.write(angle);
                  delay(30);
                }
              }
              Serial.print(" 1 ");
              servo1.write(100);
              servo3.write(30);
              delay(10000);
               
              
    
              Motor_Stop();
              delay(10000);
              
}
