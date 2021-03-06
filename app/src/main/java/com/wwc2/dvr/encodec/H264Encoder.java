package com.wwc2.dvr.encodec;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;


public class H264Encoder
{
	private final static String TAG = "MeidaCodec";
	
	private int TIMEOUT_USEC = 12000;

	private MediaCodec mediaCodec;
	int m_width;
	int m_height;
	int m_framerate;
	byte[] m_info = null;
	 
	public byte[] configbyte;

	private  int yuvqueuesize = 10;

	public  ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);

	private H264Dispatch mH264Dispatch;


	@SuppressLint("NewApi")
	public H264Encoder(int width, int height, int framerate, int bitrate, H264Dispatch mH264Dispatch) {
		
		m_width  = width;
		m_height = height;
		m_framerate = framerate;
		this.mH264Dispatch =mH264Dispatch;
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
	    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
	    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
	    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
	    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
	    try {
			mediaCodec = MediaCodec.createEncoderByType("video/avc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    mediaCodec.start();
	}
	

	@SuppressLint("NewApi")
	private void StopEncoder() {
	    try {
	        mediaCodec.stop();
	        mediaCodec.release();
	    } catch (Exception e){
	        e.printStackTrace();
	    }
	}
	
	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;

	public boolean isRuning = false;
	
	public void StopThread(){
		isRuning = false;
	}
	
	int count = 0;

	public void StartEncoderThread(){
		Thread EncoderThread = new Thread(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				isRuning = true;
				byte[] input = null;
				long pts =  0;
				long generateIndex = 0;

				while (isRuning) {
					if (YUVQueue.size() >0){
						input = YUVQueue.poll();
						byte[] yuv420sp = new byte[m_width*m_height*3/2];
						NV21ToNV12(input,yuv420sp,m_width,m_height);
						input = yuv420sp;
					}
					if (input != null) {
						try {
							long startMs = System.currentTimeMillis();
							ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
							ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
							int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
							if (inputBufferIndex >= 0) {
								pts = computePresentationTime(generateIndex);
								ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(input);
								mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
								generateIndex += 1;
							}
							
							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
							int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							while (outputBufferIndex >= 0) {
								//Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
								ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								byte[] outData = new byte[bufferInfo.size];
								outputBuffer.get(outData);
								if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
									configbyte = new byte[bufferInfo.size];
									configbyte = outData;
								}else if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME){
									byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
									System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
									System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
									mH264Dispatch.outputCodecWrite(keyframe);
									//outputStream.write(keyframe, 0, keyframe.length);
								}else{
									//mH264Dispatch.outputStream.write(outData, 0, outData.length);
									mH264Dispatch.outputCodecWrite(outData);
								}

								mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
								outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							}

						} catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		EncoderThread.start();
		
	}
	
	private void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height){
		if(nv21 == null || nv12 == null) {
			return;
		}
		int framesize = width*height;
		int i = 0,j = 0;
		System.arraycopy(nv21, 0, nv12, 0, framesize);
		for(i = 0; i < framesize; i++){
			nv12[i] = nv21[i];
		}
		for (j = 0; j < framesize/2; j+=2)
		{
		  nv12[framesize + j-1] = nv21[j+framesize];
		}
		for (j = 0; j < framesize/2; j+=2)
		{
		  nv12[framesize + j] = nv21[j+framesize-1];
		}
	}
	
    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }

	public void putYUVData(byte[] buffer, int length) {
		if (YUVQueue.size() >= 10) {
			YUVQueue.poll();
		}
		YUVQueue.add(buffer);
	}


}
