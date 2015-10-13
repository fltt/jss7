#include <org_mobicents_ss7_hardware_dahdi_Channel.h>
#include <org_mobicents_ss7_hardware_dahdi_Selector.h>
#include <dahdi/user.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <sys/time.h>
#include <sys/poll.h>

#define ZAP_NUM_BUF 4

int fd[16];
struct pollfd fds[16];

int channel_count;

static int setnonblock_fd(int fd) {
    int res, flags;
    res = fcntl(fd, F_GETFL);
    
    if (res < 0) {
	return -1;
    }
    
    flags = res | O_NONBLOCK;
    
    res = fcntl(fd, F_SETFL, flags);
    if (res < 0) {
	return -1;
    }
    
    return 0;	    	
}

static int openFileChannel(jint zapid,jint ioBufferSize) {
    struct dahdi_bufferinfo bi;
    char devname[100];
    int fd, res;
#if 0
    fd = open("/dev/dahdi/channel", O_RDWR);
    if (fd < 0)
        return -1;
    res = ioctl(fd, DAHDI_SPECIFY, &zapid);
    if(res<0)
        return -1;
#else
    int span, channel;
    span = zapid / 31;
    channel = zapid % 31;
    snprintf(devname, sizeof(devname), "/dev/dahdi/chan/%0.3d/%0.3d", span + 1, channel);
    fd = open(devname, O_RDWR);
    if (fd < 0)
        return -1;
#endif
    bi.txbufpolicy = DAHDI_POLICY_IMMEDIATE;
    bi.rxbufpolicy = DAHDI_POLICY_IMMEDIATE;
    bi.numbufs = ZAP_NUM_BUF;
    bi.bufsize = ioBufferSize;
    ioctl(fd, DAHDI_SET_BUFINFO, &bi);
    res = setnonblock_fd(fd);
    if (res < 0)
        return -1;
    res = ioctl(fd, DAHDI_SET_BLOCKSIZE, &ioBufferSize);
    if (res < 0)
        return -1;
    return fd;
}

JNIEXPORT void JNICALL Java_org_mobicents_ss7_hardware_dahdi_Selector_doRegister (JNIEnv *env, jobject obj, jint fd) {
    int i = channel_count;
    channel_count = channel_count + 1;
    fds[i].fd = fd;
    fds[i].events = POLLIN|POLLPRI|POLLOUT;
}

JNIEXPORT void JNICALL Java_org_mobicents_ss7_hardware_dahdi_Selector_doUnregister (JNIEnv *env, jobject obj, jint fd) {
    struct pollfd temp[16];
    
    int i;
    int k=0;
    
    int found=0;
    for (i = 0; i < channel_count; i++) {
	if (fds[i].fd != fd) {
	    temp[k++] = fds[i];
	}
	else
 	    found=1;
    }
    
    if(found==0)
       return;

    channel_count = channel_count - 1;
    
    for (i = 0; i < channel_count; i++) {
	fds[i] = temp[i];
    }    
}

JNIEXPORT void JNICALL Java_org_mobicents_ss7_hardware_dahdi_Channel_doRegister (JNIEnv *env, jobject obj, jint fd) {
    int i = channel_count;
    channel_count = channel_count + 1;
    fds[i].fd = fd;
    fds[i].events = POLLIN|POLLPRI|POLLOUT;
}

JNIEXPORT void JNICALL Java_org_mobicents_ss7_hardware_dahdi_Channel_doUnregister (JNIEnv *env, jobject obj, jint fd) {
    struct pollfd temp[16];
    
    int i;
    int k=0;
    
    int found=0;
    for (i = 0; i < channel_count; i++) {
	if (fds[i].fd != fd) {
	    temp[k++] = fds[i];
	}
	else
 	    found=1;
    }
    
    if(found==0)
       return;

    channel_count = channel_count - 1;
    
    for (i = 0; i < channel_count; i++) {
	fds[i] = temp[i];
    }    
}

JNIEXPORT jint JNICALL Java_org_mobicents_ss7_hardware_dahdi_Selector_doPoll (JNIEnv *env, jobject obj, jintArray selected, jint flags, jint timeout) {
    int res;
    jint *elements = (*env)->GetIntArrayElements(env, selected, 0);
    
    int s;
    int k = 0;
    int lflags = flags;

    if ( (lflags & 0x1) ) {
	    s = POLLIN;
    } 
    if ( (lflags & 0x2 ) ) {
	    s = s | POLLOUT;
    } 
    res = poll(fds, channel_count, timeout);
    if (res > 0) {
		int i;
		for (i = 0; i < channel_count; i++) {
		    if (fds[i].revents & s) {
				elements[k++] = fds[i].fd;
		    }	    
		}
    }
    (*env)->ReleaseIntArrayElements(env, selected, elements, 0);
    return k;
}

JNIEXPORT jint JNICALL Java_org_mobicents_ss7_hardware_dahdi_Channel_openChannel
  (JNIEnv *env, jobject obj, jint zapid, jint ioBufferSize) {
    return openFileChannel(zapid,ioBufferSize);
}



/*
 * Class:     org_mobicents_media_server_impl_resource_zap_Schannel
 * Method:    read
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_org_mobicents_ss7_hardware_dahdi_Channel_readData
  (JNIEnv *env, jobject obj, jint fd, jbyteArray buff, jint ioBufferSize) {
    int res;
    int errorCount = 0;
    jbyte *elements = (*env)->GetByteArrayElements(env, buff, 0);
    for (;;) {
        res = read(fd, elements, ioBufferSize);
        if (res < 0) {
            switch (errno) {
            case EINTR:
                if (++errorCount < 10)
                    continue;
                break;
            case EAGAIN:
                // case EWOULDBLOCK:
                res = 0;
            }
        }
        break;
    }
    (*env)->ReleaseByteArrayElements(env, buff, elements, 0);
    return res;
}
    



/*
 * Class:     org_mobicents_media_server_impl_resource_zap_Schannel
 * Method:    write
 * Signature: ([BI)V
 */
JNIEXPORT jint JNICALL Java_org_mobicents_ss7_hardware_dahdi_Channel_writeData
  (JNIEnv *env, jobject obj, jint fd, jbyteArray buff, jint length) {
    int res;
    int errorCount = 0;
    jbyte *elements = (*env)->GetByteArrayElements(env, buff, 0);
    for (;;) {
        res = write(fd, elements, length);
        if (res < 0) {
            switch (errno) {
            case EINTR:
                if (++errorCount < 10)
                    continue;
                break;
            case EAGAIN:
                // case EWOULDBLOCK:
                res = 0;
            }
        }
        break;
    }
    (*env)->ReleaseByteArrayElements(env, buff, elements, 0);
    return res;
}   
 
 

/*
 * Class:     org_mobicents_media_server_impl_resource_zap_Schannel
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_mobicents_ss7_hardware_dahdi_Channel_closeChannel (JNIEnv *env, jobject obj, jint fd) {
    close(fd);
}


