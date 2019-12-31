#include <stm32f10x.h>
#include "usart.h"
#include "DHT11.h"
#include "delay.h"
#include <stdio.h>
#include <stdlib.h>

void NVIC_Configuration(void)
{
  NVIC_InitTypeDef NVIC_InitStructure;	
  
  NVIC_SetVectorTable(NVIC_VectTab_FLASH, 0x0000);

  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_1);

  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 1;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;  
  NVIC_Init(&NVIC_InitStructure);
}

int main(void)
{
	u8 temperature;  	    
	u8 humidity; 
	SystemInit();
	usart_Configuration();	
	NVIC_Configuration();
	delay_init();
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA,ENABLE);
	printf("Initializing...\r\n");
	while(DHT11_Init());
    printf("Initialization Complete!");
  	while(1)
	{
		DHT11_Read_Data(&temperature,&humidity);	
		printf("temperature=%d\r\n",temperature);
		printf("humidity=%d\r\n",humidity);
 		delay_ms(100);
	}
}

