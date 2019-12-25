#include "ds18b20.h"

static void DS18B20_GPIO_Config(void)
{		
		GPIO_InitTypeDef GPIO_InitStructure;//定义一个GPIO_InitTypeDef类型的结构体

		RCC_APB2PeriphClockCmd(DS18B20_CLK, ENABLE); //开启DS18B20_PORT的外设时钟

  	GPIO_InitStructure.GPIO_Pin = DS18B20_PIN;//选择要控制的DS18B20_PORT引脚	

  	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; //设置引脚模式为普通推挽输出  

  	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;//设置引脚速率为50MHz 

  	GPIO_Init(DS18B20_PORT, &GPIO_InitStructure);//调用库函数，初始化DS18B20_PORT
	
		GPIO_SetBits(DS18B20_PORT, DS18B20_PIN);	// 拉高DS18B20信号端的电平 
}

static void DS18B20_Mode_IPU(void)
{
 	  GPIO_InitTypeDef GPIO_InitStructure;

	  GPIO_InitStructure.GPIO_Pin = DS18B20_PIN;//选择要控制的DS18B20_PORT引脚

	  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;	//设置引脚模式为浮空输入模式

	  GPIO_Init(DS18B20_PORT, &GPIO_InitStructure);//调用库函数，初始化DS18B20_PORT
}

static void DS18B20_Mode_Out_PP(void)
{
		GPIO_InitTypeDef GPIO_InitStructure;

  	GPIO_InitStructure.GPIO_Pin = DS18B20_PIN;//选择要控制的DS18B20_PORT引脚

  	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; //设置引脚模式为通用推挽输出  

  	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;//设置引脚速率为50MHz

  	GPIO_Init(DS18B20_PORT, &GPIO_InitStructure);//调用库函数，初始化DS18B20_PORT
}

/*主机给从机发送复位脉冲*/
static void DS18B20_Rst(void)
{
	DS18B20_Mode_Out_PP();//主机设置为普通推挽输出
	
	DS18B20_DATA_OUT(LOW);

	Delay_us(750);//主机至少产生480us的低电平复位信号
	
	DS18B20_DATA_OUT(HIGH);//主机在产生复位信号后，需将总线拉高
	
	Delay_us(15);//从机接收到主机的复位信号后，会在15~60us后给主机发一个存在脉冲
}

/*
 * 检测从机给主机返回的存在脉冲
 * 0：成功
 * 1：失败
 */
static uint8_t DS18B20_Presence(void)
{
	uint8_t pulse_time = 0;
	
	DS18B20_Mode_IPU();//主机设置为上拉输入
	
	/* 等待存在脉冲的到来，存在脉冲为一个60~240us的低电平信号 
	 * 如果存在脉冲没有来则做超时处理，从机接收到主机的复位信号后，会在15~60us后给主机发一个存在脉冲
	 */
	while( DS18B20_DATA_IN() && pulse_time<200 )
	{
		pulse_time++;
		Delay_us(1);
	}	
	/* 经过100us后，存在脉冲都还没有到来*/
	if( pulse_time >=100 )
		return 1;
	else
		pulse_time = 0;
	
	/* 存在脉冲到来，且存在的时间不能超过240us */
	while( !DS18B20_DATA_IN() && pulse_time<240 )
	{
		pulse_time++;
		Delay_us(1);
	}	
	if( pulse_time >=240 )
			return 1;
	else
			return 0;
}

/* 从DS18B20读取一个bit */
static uint8_t DS18B20_Read_Bit(void)
{
	uint8_t dat;
	
	DS18B20_Mode_Out_PP();//读0和读1的时间至少要大于60us

	DS18B20_DATA_OUT(LOW);//读时间的起始：必须由主机产生 >1us <15us 的低电平信号
	
	Delay_us(10);
	
	DS18B20_Mode_IPU();//设置成输入，释放总线，由外部上拉电阻将总线拉高
	
	if( DS18B20_DATA_IN() == SET )
		dat = 1;
	else
		dat = 0;
	
	Delay_us(45);//这个延时参数请参考时序图
	
	return dat;
}

/*
 * 从DS18B20读一个字节，低位先行
 */
uint8_t DS18B20_Read_Byte(void)
{
	uint8_t i, j, dat = 0;	
	
	for(i=0; i<8; i++) 
	{
		j = DS18B20_Read_Bit();		
		dat = (dat) | (j<<i);
	}
	
	return dat;
}

/*
 * 写一个字节到DS18B20，低位先行
 */
void DS18B20_Write_Byte(uint8_t dat)
{
	uint8_t i, testb;
	DS18B20_Mode_Out_PP();//将引脚设置为普通推挽输出
	
	for( i=0; i<8; i++ )
	{
		testb = dat&0x01;
		dat = dat>>1;		
		/* 写0和写1的时间至少要大于60us */
		if (testb)
		{			
			DS18B20_DATA_OUT(LOW);
			/* 1us < 这个延时 < 15us */
			Delay_us(8);
			
			DS18B20_DATA_OUT(HIGH);
			Delay_us(45);
		}		
		else
		{			
			DS18B20_DATA_OUT(LOW);
			/* 60us < Tx 0 < 120us */
			Delay_us(70);
			
			DS18B20_DATA_OUT(HIGH);			
			/* 1us < Trec(恢复时间) < 无穷大*/
			Delay_us(2);
		}
	}
}

void DS18B20_Start(void)
{
	DS18B20_Rst();	   
	DS18B20_Presence();	 
	DS18B20_Write_Byte(0XCC);		/* 跳过 ROM */
	DS18B20_Write_Byte(0X44);		/* 开始转换 */
}

uint8_t DS18B20_Init(void)
{
	DS18B20_GPIO_Config();
	DS18B20_Rst();
	
	return DS18B20_Presence();
}

float DS18B20_Get_Temp(void)
{
	uint8_t tpmsb, tplsb;
	short s_tem;
	float f_tem;
	
	DS18B20_Rst();	   
	DS18B20_Presence();	 
	DS18B20_Write_Byte(0XCC);				/* 跳过 ROM */
	DS18B20_Write_Byte(0X44);				/* 开始转换 */
	
	DS18B20_Rst();
  DS18B20_Presence();
	DS18B20_Write_Byte(0XCC);				/* 跳过 ROM */
  DS18B20_Write_Byte(0XBE);				/* 读温度值 */
	
	tplsb = DS18B20_Read_Byte();		 
	tpmsb = DS18B20_Read_Byte(); 
	
	s_tem = tpmsb<<8;
	s_tem = s_tem | tplsb;
	
	if( s_tem < 0 )		/* 负温度 */
		f_tem = (~s_tem+1) * 0.0625;	
	else
		f_tem = s_tem * 0.0625;
	
	return f_tem; 	
}
