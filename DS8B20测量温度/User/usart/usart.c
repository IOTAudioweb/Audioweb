#include "usart.h"

void USART1_Config(void)
{
		GPIO_InitTypeDef GPIO_InitStructure;
		USART_InitTypeDef USART_InitStructure;
		
		/* config USART1 clock */
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1 | RCC_APB2Periph_GPIOA, ENABLE);//��ʼusart1��GPIOA����ʱ��
		
		/* USART1 GPIO config */
		/* Configure USART1 Tx (PA.09) as alternate function push-pull */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;               //ѡ������PA9(TXD)
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;         //���ø����������
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;       //�������ŵ�����Ϊ50MHz
		GPIO_Init(GPIOA, &GPIO_InitStructure);
	
		/* Configure USART1 Rx (PA.10) as input floating */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;               //ѡ������PA10(RXD)
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;    //���ø�������ģʽ
		GPIO_Init(GPIOA, &GPIO_InitStructure);
			
		/* USART1 mode config */
		USART_InitStructure.USART_BaudRate = 115200;                 //���ò�����Ϊ115200
		USART_InitStructure.USART_WordLength = USART_WordLength_8b;  //�������ݳ���Ϊ8bit
		USART_InitStructure.USART_StopBits = USART_StopBits_1;       //����ֹͣλ
		USART_InitStructure.USART_Parity = USART_Parity_No ;         //��������żУ��λ
		USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;//������Ӳ����
		USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;  //ѡ�����ŵĵڶ�����
		USART_Init(USART1, &USART_InitStructure);                      
		USART_Cmd(USART1, ENABLE);
}

///�ض���c�⺯��printf��USART1
int fputc(int ch, FILE *f)
{
		/* ����һ���ֽ����ݵ�USART1 */
		USART_SendData(USART1, (uint8_t) ch);
		
		/* �ȴ�������� */
		while (USART_GetFlagStatus(USART1, USART_FLAG_TC) == RESET);		
	
		return (ch);
}

///�ض���c�⺯��scanf��USART1
int fgetc(FILE *f)
{
		/* �ȴ�����1�������� */
		while (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) == RESET);

		return (int)USART_ReceiveData(USART1);
}
/*********************************************END OF FILE**********************/
