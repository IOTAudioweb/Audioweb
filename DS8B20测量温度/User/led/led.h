#ifndef __LED_H
#define	__LED_H

#include "stm32f10x.h"

#define ON  0
#define OFF 1

/* 直接操作寄存器的方法控制IO */
#define digitalToggle(p,i)		{p->ODR ^=i;}			//输出反转状态

/* 定义控制IO的宏 */
#define LED1_TOGGLE		digitalToggle(GPIOA,GPIO_Pin_0)
void LED_GPIO_Config(void);

#endif 
