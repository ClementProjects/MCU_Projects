#include <msp430.h>
#include <string.h>

#define UART_RXD BIT1
#define UART_TXD BIT2

#define BUFFER_SIZE 10

char receiveBuffer[BUFFER_SIZE];
int receiveIndex = 0;

void UART_init() {
    P1SEL |= UART_RXD + UART_TXD; // Enable UART RXD and TXD function
    P1SEL2 |= UART_RXD + UART_TXD;

    UCA0CTL1 |= UCSSEL_2; // Use SMCLK as the clock source
    UCA0BR0 = 104; // Set baud rate to 9600 (assuming SMCLK is 1MHz)
    UCA0BR1 = 0;
    UCA0MCTL = UCBRS0; // Modulation UCBRSx = 1

    UCA0CTL1 &= ~UCSWRST; // Initialize UART state machine
    IE2 |= UCA0RXIE; // Enable UART receive interrupt
}

void UART_write(char data) {
    while (!(IFG2 & UCA0TXIFG)); // Wait until the TX buffer is empty
    UCA0TXBUF = data; // Send the data
}

void sendString(const char* str) {
    int i = 0;
    while (str[i] != '\0') {
        UART_write(str[i]);
        i++;
    }
}

void handleReceivedData(char instruction) {
    receiveBuffer[receiveIndex++] = instruction;

    if (receiveIndex >= BUFFER_SIZE) {
        // Buffer overflow, reset buffer
        receiveIndex = 0;
        return;
    }

    if (instruction == '\n') {
        receiveBuffer[receiveIndex - 1] = '\0'; // Null-terminate the received string

        char reply[20];
        if (strcmp(receiveBuffer, "01 02 01") == 0) {
            if (P2OUT & BIT3)
                sprintf(reply, "Light1 is on");
            else
                sprintf(reply, "Light1 is off");
        } else if (strcmp(receiveBuffer, "01 02 02") == 0) {
            if (P2OUT & BIT4)
                sprintf(reply, "Light2 is on");
            else
                sprintf(reply, "Light2 is off");
        } else if (strcmp(receiveBuffer, "01 02 03") == 0) {
            if (P2OUT & BIT4)
                sprintf(reply, "Potentiometer1 is on");
            else
                sprintf(reply, "Potentiometer1 is off");
        } else if (strcmp(receiveBuffer, "03 02 01 00") == 0) {
            P2OUT &= ~BIT3; // Turn off LED L1
            sprintf(reply, "03 02 CC");
        } else if (strcmp(receiveBuffer, "03 02 01 01") == 0) {
            P2OUT |= BIT3; // Turn on LED L1
            sprintf(reply, "03 02 CC");
        } else if (strcmp(receiveBuffer, "03 02 02 00") == 0) {
            P2OUT &= ~BIT4; // Turn off LED L2
            sprintf(reply, "03 02 CC");
        } else if (strcmp(receiveBuffer, "03 02 02 01") == 0) {
            P2OUT |= BIT4; // Turn on LED L2
            sprintf(reply, "03 02 CC");
        } else if (strcmp(receiveBuffer, "03 02 03 00") == 0) {
            P2OUT &= ~BIT5; // Turn off LED Potentiometer1
            sprintf(reply, "03 02 CC");
        } else if (strcmp(receiveBuffer, "03 02 03 01") == 0) {
            P2OUT |= BIT5; // Turn on LED Potentiometer1
            sprintf(reply, "03 02 CC");
        } else {
            // Invalid instruction, send an error reply
            sprintf(reply, "Invalid Instruction");
        }
        sendString(reply);
    }
}

int main(void) {
    WDTCTL = WDTPW + WDTHOLD; // Stop watchdog timer

    UART_init(); // Initialize UART communication

    __enable_interrupt(); // Enable global interrupts

    P2DIR |= BIT3 + BIT4 + BIT5;
    P2OUT |= BIT3 + BIT4 + BIT5; // Set LEDs as output and turn them on

    while (1) {
    }
}

#pragma vector=USCIAB0RX_VECTOR
__interrupt void USCI0RX_ISR(void) {
    char receivedData = UCA0RXBUF; // Read received data
    handleReceivedData(receivedData); // Handle the received data
}
