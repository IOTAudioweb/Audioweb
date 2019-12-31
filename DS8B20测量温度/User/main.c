

#include "stm32f10x.h"
#include "systick.h"
#include "led.h"
#include "usart.h"
#include "ds18b20.h"

int main(void)
{	
	SysTick_Init();//����SysTick Ϊ1us�ж�һ��

	LED_GPIO_Config();//LED �˿ڳ�ʼ��
	
	USART1_Config();	//���ô���
	printf("\r\n this is a ds18b20 test demo \r\n");
	
	GPIO_PinRemapConfig(GPIO_Remap_SWJ_Disable, ENABLE);      //����SWD��JTAG,����ʹ��PA13I/O��
	
	while( DS18B20_Init())	//���DS18B20�Ƿ����
	{
		printf("\r\n no ds18b20 exit \r\n");
		Delay_ms(1000);		/* 1s ����һ�� */
	}	
	printf("\r\n ds18b20 exit \r\n");//��⵽ds18b20���ھͷ���
	
	for(;;)
	{	
		printf("\r\n temperature %.1f\r\n",DS18B20_Get_Temp());	//�����ϴ��ڵĸ�ʽ			 
		Delay_ms(1000);		/* 1s ��ȡһ���¶�ֵ */
		LED1_TOGGLE;//ÿ����һ�Σ�LED��״̬��ȡ��
	}    
}
