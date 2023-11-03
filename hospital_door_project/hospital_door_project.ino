#include <MFRC522.h>
#include <SPI.h>

int RST_PIN = 9;
int SS_PIN = 10;

MFRC522 rfid(SS_PIN, RST_PIN);
byte ID[4] = {35,15,186,151};

void setup() {
  
  Serial.begin(9600);
  SPI.begin();
  rfid.PCD_Init();


  pinMode(8, OUTPUT);
  pinMode(7, OUTPUT);

  }

void loop() {
  
  if(! rfid.PICC_IsNewCardPresent())
    return;
  if(! rfid.PICC_ReadCardSerial())
    return;
  
  if(rfid.uid.uidByte[0] == ID[0] && 
  rfid.uid.uidByte[1] == ID[1] && 
  rfid.uid.uidByte[2] == ID[2] && 
  rfid.uid.uidByte[3] == ID[3]){
    Serial.println("You May Enter");
  digitalWrite(8, HIGH);
  delay(3000);
  digitalWrite(8, LOW);

  }
  else{
    Serial.println("Access Denied");
    printCard();
    digitalWrite(7, HIGH);
    delay(3000);
    digitalWrite(7, LOW);
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


