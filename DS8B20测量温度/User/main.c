

#include "stm32f10x.h"
#include "systick.h"
#include "led.h"
#include "usart.h"
#include "ds18b20.h"

int main(void)
{	
	SysTick_Init();//配置SysTick 为1us中断一次

	LED_GPIO_Config();//LED 端口初始化
	
	USART1_Config();	//配置串口
	printf("\r\n this is a ds18b20 test demo \r\n");
	
	GPIO_PinRemapConfig(GPIO_Remap_SWJ_Disable, ENABLE);      //禁用SWD和JTAG,才能使用PA13I/O口
	
	while( DS18B20_Init())	//检测DS18B20是否存在
	{
		printf("\r\n no ds18b20 exit \r\n");
		Delay_ms(1000);		/* 1s 发送一次 */
	}	
	printf("\r\n ds18b20 exit \r\n");//检测到ds18b20存在就发送
	
	for(;;)
	{	
		printf("\r\n temperature %.1f\r\n",DS18B20_Get_Temp());	//发送上串口的格式			 
		Delay_ms(1000);		/* 1s 读取一次温度值 */
		LED1_TOGGLE;//每发送一次，LED的状态就取反
	}    
}
