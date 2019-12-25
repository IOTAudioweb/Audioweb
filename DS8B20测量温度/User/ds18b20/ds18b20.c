#include "ds18b20.h"

static void DS18B20_GPIO_Config(void)
{		
		GPIO_InitTypeDef GPIO_InitStructure;//����һ��GPIO_InitTypeDef���͵Ľṹ��

		RCC_APB2PeriphClockCmd(DS18B20_CLK, ENABLE); //����DS18B20_PORT������ʱ��

  	GPIO_InitStructure.GPIO_Pin = DS18B20_PIN;//ѡ��Ҫ���Ƶ�DS18B20_PORT����	

  	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; //��������ģʽΪ��ͨ�������  

  	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;//������������Ϊ50MHz 

  	GPIO_Init(DS18B20_PORT, &GPIO_InitStructure);//���ÿ⺯������ʼ��DS18B20_PORT
	
		GPIO_SetBits(DS18B20_PORT, DS18B20_PIN);	// ����DS18B20�źŶ˵ĵ�ƽ 
}

static void DS18B20_Mode_IPU(void)
{
 	  GPIO_InitTypeDef GPIO_InitStructure;

	  GPIO_InitStructure.GPIO_Pin = DS18B20_PIN;//ѡ��Ҫ���Ƶ�DS18B20_PORT����

	  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;	//��������ģʽΪ��������ģʽ

	  GPIO_Init(DS18B20_PORT, &GPIO_InitStructure);//���ÿ⺯������ʼ��DS18B20_PORT
}

static void DS18B20_Mode_Out_PP(void)
{
		GPIO_InitTypeDef GPIO_InitStructure;

  	GPIO_InitStructure.GPIO_Pin = DS18B20_PIN;//ѡ��Ҫ���Ƶ�DS18B20_PORT����

  	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; //��������ģʽΪͨ���������  

  	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;//������������Ϊ50MHz

  	GPIO_Init(DS18B20_PORT, &GPIO_InitStructure);//���ÿ⺯������ʼ��DS18B20_PORT
}

/*�������ӻ����͸�λ����*/
static void DS18B20_Rst(void)
{
	DS18B20_Mode_Out_PP();//��������Ϊ��ͨ�������
	
	DS18B20_DATA_OUT(LOW);

	Delay_us(750);//�������ٲ���480us�ĵ͵�ƽ��λ�ź�
	
	DS18B20_DATA_OUT(HIGH);//�����ڲ�����λ�źź��轫��������
	
	Delay_us(15);//�ӻ����յ������ĸ�λ�źź󣬻���15~60us���������һ����������
}

/*
 * ���ӻ����������صĴ�������
 * 0���ɹ�
 * 1��ʧ��
 */
static uint8_t DS18B20_Presence(void)
{
	uint8_t pulse_time = 0;
	
	DS18B20_Mode_IPU();//��������Ϊ��������
	
	/* �ȴ���������ĵ�������������Ϊһ��60~240us�ĵ͵�ƽ�ź� 
	 * �����������û����������ʱ�����ӻ����յ������ĸ�λ�źź󣬻���15~60us���������һ����������
	 */
	while( DS18B20_DATA_IN() && pulse_time<200 )
	{
		pulse_time++;
		Delay_us(1);
	}	
	/* ����100us�󣬴������嶼��û�е���*/
	if( pulse_time >=100 )
		return 1;
	else
		pulse_time = 0;
	
	/* �������嵽�����Ҵ��ڵ�ʱ�䲻�ܳ���240us */
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

/* ��DS18B20��ȡһ��bit */
static uint8_t DS18B20_Read_Bit(void)
{
	uint8_t dat;
	
	DS18B20_Mode_Out_PP();//��0�Ͷ�1��ʱ������Ҫ����60us

	DS18B20_DATA_OUT(LOW);//��ʱ�����ʼ���������������� >1us <15us �ĵ͵�ƽ�ź�
	
	Delay_us(10);
	
	DS18B20_Mode_IPU();//���ó����룬�ͷ����ߣ����ⲿ�������轫��������
	
	if( DS18B20_DATA_IN() == SET )
		dat = 1;
	else
		dat = 0;
	
	Delay_us(45);//�����ʱ������ο�ʱ��ͼ
	
	return dat;
}

/*
 * ��DS18B20��һ���ֽڣ���λ����
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
 * дһ���ֽڵ�DS18B20����λ����
 */
void DS18B20_Write_Byte(uint8_t dat)
{
	uint8_t i, testb;
	DS18B20_Mode_Out_PP();//����������Ϊ��ͨ�������
	
	for( i=0; i<8; i++ )
	{
		testb = dat&0x01;
		dat = dat>>1;		
		/* д0��д1��ʱ������Ҫ����60us */
		if (testb)
		{			
			DS18B20_DATA_OUT(LOW);
			/* 1us < �����ʱ < 15us */
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
			/* 1us < Trec(�ָ�ʱ��) < �����*/
			Delay_us(2);
		}
	}
}

void DS18B20_Start(void)
{
	DS18B20_Rst();	   
	DS18B20_Presence();	 
	DS18B20_Write_Byte(0XCC);		/* ���� ROM */
	DS18B20_Write_Byte(0X44);		/* ��ʼת�� */
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
	DS18B20_Write_Byte(0XCC);				/* ���� ROM */
	DS18B20_Write_Byte(0X44);				/* ��ʼת�� */
	
	DS18B20_Rst();
  DS18B20_Presence();
	DS18B20_Write_Byte(0XCC);				/* ���� ROM */
  DS18B20_Write_Byte(0XBE);				/* ���¶�ֵ */
	
	tplsb = DS18B20_Read_Byte();		 
	tpmsb = DS18B20_Read_Byte(); 
	
	s_tem = tpmsb<<8;
	s_tem = s_tem | tplsb;
	
	if( s_tem < 0 )		/* ���¶� */
		f_tem = (~s_tem+1) * 0.0625;	
	else
		f_tem = s_tem * 0.0625;
	
	return f_tem; 	
}
