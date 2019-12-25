#ifndef __LED_H
#define	__LED_H

#include "stm32f10x.h"

#define ON  0
#define OFF 1

/* ֱ�Ӳ����Ĵ����ķ�������IO */
#define digitalToggle(p,i)		{p->ODR ^=i;}			//�����ת״̬

/* �������IO�ĺ� */
#define LED1_TOGGLE		digitalToggle(GPIOA,GPIO_Pin_0)
void LED_GPIO_Config(void);

#endif 
