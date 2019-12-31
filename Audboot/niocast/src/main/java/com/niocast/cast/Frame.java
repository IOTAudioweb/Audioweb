/**  
 * @Title:  Frame.java   
 * @Package com.niocast.cast   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @author: Shuofang     
 * @date:   2019年1月26日 下午2:36:03   
 * @version V1.0 
 * @Copyright: 2019 
 */
package com.niocast.cast;

import java.nio.ByteBuffer;

/**
 * @author Shuofang
 *	TODO	Frame 类
 * 	包括 构造函数、设置、排序函数
 */
public class Frame  implements Comparable<Frame> {
	/**
	 * Frame 类
	 * 包括 构造函数、设置、排序函数
	 */
	public byte[] data;
	public int length;
	public long timeStampMs;
	public Frame(ByteBuffer databuf) {
		try {
			data = new byte[databuf.remaining()-13];
			databuf.get(data);
			byte[] timebyte = new byte[databuf.remaining()];
			databuf.get(timebyte);
			timeStampMs = Long.parseLong(new String(timebyte));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			data = new byte[0];
			timeStampMs = System.currentTimeMillis();
		}
	}
	public Frame(byte[] data, int offset, int length) {
		this.data = new byte[length];
		System.arraycopy(data, offset, this.data, 0, length);
		this.length = length;
		this.timeStampMs = System.currentTimeMillis();
	}
	public void set(byte[] data, int offset, int length, int timeStampMs) {
		if (this.data.length < length) {
			this.data = new byte[length];
		}

		System.arraycopy(data, offset, this.data, 0, length);

		this.length = length;
		this.timeStampMs = timeStampMs;
	}

	/*public static void sortFrame(List<Frame> frameQueue) {
		Collections.sort(frameQueue, new Comparator<Frame>() {
			@Override
			public int compare(Frame left, Frame right) {
				if (left.timeStampMs < right.timeStampMs) {
					return -1;
				} else if (left.timeStampMs == right.timeStampMs) {
					return 0;
				} else {
					return 1;
				}
			}
		});
	}*/
	 @Override
    public int compareTo(Frame o) {
        //降序
        //return (int)(o.timeStampMs - this.timeStampMs);
        //升序
        return (int)(this.timeStampMs - o.timeStampMs);        
    }
}
