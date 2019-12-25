#include "led.h"   

void LED_GPIO_Config(void)
{		
		GPIO_InitTypeDef GPIO_InitStructure;//����һ��GPIO_InitTypeDef���͵Ľṹ��

		RCC_APB2PeriphClockCmd( RCC_APB2Periph_GPIOA|RCC_APB2Periph_AFIO, ENABLE); //����LED������ʱ��

		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0;	//ѡ��Ҫ���Ƶ�GPIOB����

		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;  //��������ģʽΪͨ��������� 

		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz; //������������Ϊ50MHz

		GPIO_Init(GPIOA, &GPIO_InitStructure);//���ÿ⺯������ʼ��GPIOB0	
		
		GPIO_SetBits(GPIOA, GPIO_Pin_0);//�ر�����led��
		
}
