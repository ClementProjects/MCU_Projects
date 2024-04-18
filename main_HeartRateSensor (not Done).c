#include <msp430.h>
#include <stdio.h>

#define MAX_SAMPLES 5 // Maximum number of heart rate samples to calculate BPM
#define SAMPLE_PERIOD 1000 // Time interval between heart rate samples (in milliseconds)

volatile unsigned int samples[MAX_SAMPLES];
volatile unsigned int sampleIndex = 0;

void initTimer()
{
    TA0CTL |= TACLR; // Clear timer
    TA0CTL = TASSEL_2 + MC_1 + ID_3; // Timer source: SMCLK (1 MHz), Mode: Up, Clock divider: 8
    TA0CCR0 = SAMPLE_PERIOD - 1; // Set the timer period
    TA0CCTL0 = CCIE; // Enable timer interrupt
}

void initUART()
{
    P1SEL |= BIT1 + BIT2; // Select UART TX/RX function on P1.1 and P1.2
    P1SEL2 |= BIT1 + BIT2;

    UCA0CTL1 |= UCSWRST; // Reset UART state
    UCA0CTL1 |= UCSSEL_2; // Use SMCLK as the clock source (1 MHz)
    UCA0BR0 = 104; // Set Baud Rate to 9600 (1 MHz / 9600 = 104.1667)
    UCA0BR1 = 0;
    UCA0MCTL = UCBRS0; // Modulation UCBRSx = 1
    UCA0CTL1 &= ~UCSWRST; // Release UART state
    IE2 |= UCA0RXIE; // Enable UART RX interrupt
}

void sendHeartRateData(unsigned int bpmValue)
{
    // Convert BPM value to string
    char dataStr[5];
    sprintf(dataStr, "%c", bpmValue);

    // Send data via UART
    int i = 0;
    while (dataStr[i] != '\0')
    {
        while (!(IFG2 & UCA0TXIFG)); // Wait until TX buffer is ready
        UCA0TXBUF = dataStr[i]; // Send character
        i++;
    }
    while (!(IFG2 & UCA0TXIFG)); // Wait until TX buffer is ready
    UCA0TXBUF = '\n'; // Send newline character (to indicate end of data)
}

void initADC()
{
    ADC10CTL1 = INCH_0 + SHS_0 + ADC10DIV_7 + ADC10SSEL_0; // Select Channel 0 (A0), Clock divider: 8, Clock source: ADC10OSC
    ADC10AE0 |= 0x01; // Enable analog input on A0
    ADC10CTL0 = SREF_0 + ADC10SHT_3 + ADC10ON + ADC10IE; // Vref: AVCC, Sample and hold time: 64 x ADC10CLKs, ADC on, ADC interrupt enabled
}

int main(void)
{
    WDTCTL = WDTPW + WDTHOLD; // Stop watchdog timer
    BCSCTL1 = CALBC1_1MHZ; // Set DCO to 1MHz
    DCOCTL = CALDCO_1MHZ;

    P1DIR |= BIT0; // Set P1.0 as an output (for the LED)
    initTimer();
    initADC();
    initUART();

    __enable_interrupt();

    while (1)
    {
        // Main program loop
    }
}

volatile float voltage = 0.0;

#pragma vector=TIMER0_A0_VECTOR
__interrupt void Timer_A(void)
{
    samples[sampleIndex] = ADC10MEM; // Store ADC value in samples array
    sampleIndex++;

    if (sampleIndex >= MAX_SAMPLES)
    {
        unsigned int j;
        unsigned int sum = 0;
        for (j=0; j <= MAX_SAMPLES; j++)
        {
            sum += samples[j];
        }
        float averageVoltage = (sum / (float)MAX_SAMPLES) * (3.3 / 1023.0); // Convert the average ADC value to voltage (assuming Vcc = 3.3V)
        float heartRate = averageVoltage * 100.0; // Convert voltage to heart rate (BPM)

        if (heartRate >= 60 && heartRate <= 100)
        {
            P1OUT |= BIT0; // Turn on the LED if the heart rate is within a normal range (e.g., 60-100 bpm)
        }
        else
        {
            P1OUT &= ~BIT0; // Turn off the LED if the heart rate is outside the normal range
        }

        sendHeartRateData((unsigned int)heartRate);

        sampleIndex = 0; // Reset sample index
    }
}
