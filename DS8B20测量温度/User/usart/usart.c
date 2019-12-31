#include "usart.h"

void USART1_Config(void)
{
		GPIO_InitTypeDef GPIO_InitStructure;
		USART_InitTypeDef USART_InitStructure;
		
		/* config USART1 clock */
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1 | RCC_APB2Periph_GPIOA, ENABLE);//开始usart1和GPIOA外设时钟
		
		/* USART1 GPIO config */
		/* Configure USART1 Tx (PA.09) as alternate function push-pull */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;               //选择引脚PA9(TXD)
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;         //设置复用推挽输出
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;       //设置引脚的速率为50MHz
		GPIO_Init(GPIOA, &GPIO_InitStructure);
	
		/* Configure USART1 Rx (PA.10) as input floating */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;               //选择引脚PA10(RXD)
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;    //设置浮空输入模式
		GPIO_Init(GPIOA, &GPIO_InitStructure);
			
		/* USART1 mode config */
		USART_InitStructure.USART_BaudRate = 115200;                 //设置波特率为115200
		USART_InitStructure.USART_WordLength = USART_WordLength_8b;  //设置数据长度为8bit
		USART_InitStructure.USART_StopBits = USART_StopBits_1;       //设置停止位
		USART_InitStructure.USART_Parity = USART_Parity_No ;         //不设置奇偶校验位
		USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;//不设置硬件流
		USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;  //选用引脚的第二功能
		USART_Init(USART1, &USART_InitStructure);                      
		USART_Cmd(USART1, ENABLE);
}

///重定向c库函数printf到USART1
int fputc(int ch, FILE *f)
{
		/* 发送一个字节数据到USART1 */
		USART_SendData(USART1, (uint8_t) ch);
		
		/* 等待发送完毕 */
		while (USART_GetFlagStatus(USART1, USART_FLAG_TC) == RESET);		
	
		return (ch);
}

///重定向c库函数scanf到USART1
int fgetc(FILE *f)
{
		/* 等待串口1输入数据 */
		while (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) == RESET);

		return (int)USART_ReceiveData(USART1);
}
/*********************************************END OF FILE**********************/
