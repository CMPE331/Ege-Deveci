/*
This is a project for İstanbul Bilgi University Computer Engineering Department
CMPE 331 - Group 6

NFC Authenticated door lock to integrate with an appointment system.
Intended to be integrated to a hospitals' appointment system for
patient management on the policlinic queues.

Metehan Karaca
*/

#include <MFRC522.h> //Library for the MFRC522 reader
#include <SPI.h> //Library for peripherals

int RST_PIN = 9; //Reset pin of the rfid module
int SS_PIN = 10; //SS pin of the rfid module

int readPin = 2; // Input pin for the read mode button

/*
Read Mode: In this version of the prototype there is a "read mode" that
is used to add authenticated cards' UID'S to the fifo card queue where they
will open the lock if read by the antenna in the normal mode. The UID's used
will leave the queue.

The read mode is indicated by the blue led Light
*/

//Pins for the RGB led
int redLed = 3;
int greenLed = 6;
int blueLed = 5;

MFRC522 rfid(SS_PIN, RST_PIN); //Initialise RFID reader

byte ID[4] = {62, 95, 154, 202};//Sample NFC card UID for testing purposes

byte personnel[4][100];

bool readMode = false;


int lastReadMode;
unsigned long lastDebounceTime = 0; // Time of the last button state change
unsigned long debounceDelay = 50;   // Debounce time in milliseconds

void setup() {

  Serial.begin(9600); //Begin usb connection to the pc serial monitor
  
  SPI.begin();
  rfid.PCD_Init();

  pinMode(readPin, INPUT);

  pinMode(redLed, OUTPUT);
  pinMode(greenLed, OUTPUT);
  pinMode(blueLed, OUTPUT);


//Opening Light Animations ------------
for(int i=0; i< 250; i++){
  analogWrite(blueLed, i);
  delay(3);
}
delay(30);
for(int i=0; i< 250; i++){
  analogWrite(redLed, i);
  if(i<125){
    analogWrite(blueLed, 250 - i*2);
  }
  delay(2);
}
delay(150);
for(int i=0; i< 250; i++){
  analogWrite(greenLed, i);
  if(i<125){
    analogWrite(redLed, 250 - i*2);
  }
  delay(4);
}
  delay(40);

  digitalWrite(redLed, 0);
  digitalWrite(blueLed, 0);
  digitalWrite(greenLed, 0);
//---------------------------------------

  Serial.println("Setup Done");
}

void loop() {

  //Code block for switching between normal mode and read mode using the button
  int read = 0;
  read = digitalRead(readPin);
  if(read == 1){
    while(read == 1){
      read = digitalRead(readPin);
    }
    readMode = !readMode;
    Serial.println("readmode changed");
  }

  //Toggle blue light to indicate read mode
  if(readMode == true){
   digitalWrite(blueLed, HIGH);
  }
  if(readMode == false){
     digitalWrite(blueLed, LOW);
  }


  if(readMode == false){
    //Normal mode instructions

    if(! rfid.PICC_IsNewCardPresent())
    return;
    if(! rfid.PICC_ReadCardSerial())
    return;
  
    if(rfid.uid.uidByte[0] == ID[0] && 
    rfid.uid.uidByte[1] == ID[1] && 
    rfid.uid.uidByte[2] == ID[2] && 
    rfid.uid.uidByte[3] == ID[3]){
    Serial.println("You May Enter");
    printCard();
    digitalWrite(greenLed, HIGH);
    delay(1200);
    digitalWrite(greenLed, LOW);

  }
  else{
    Serial.println("Access Denied");
    printCard();
    digitalWrite(redLed, HIGH);
    delay(1200);
    digitalWrite(redLed, LOW);
  }
  }


  if(readMode== true){
    //Readmode instructions
    if(! rfid.PICC_IsNewCardPresent())
    return;
    if(! rfid.PICC_ReadCardSerial())
    return;

    ID[0] = rfid.uid.uidByte[0];
    ID[1] = rfid.uid.uidByte[1];
    ID[2] = rfid.uid.uidByte[2];
    ID[3] = rfid.uid.uidByte[3];

    rfid.PICC_HaltA();

  }

  
  rfid.PICC_HaltA();

}
void printCard(){
  Serial.print("Card UID : ");
  for(int i=0; i <=3; i++){
     Serial.print(rfid.uid.uidByte[i]);
     Serial.print(" ");
  }
  Serial.println("");
}


