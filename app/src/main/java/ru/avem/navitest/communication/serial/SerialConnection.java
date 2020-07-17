package ru.avem.navitest.communication.serial;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static ru.avem.navitest.MainActivity.ACTION_USB_PERMISSION;

public class SerialConnection {
    private static final String TAG = "StatusActivity";

    private final Context mContext;
    private UsbManager mUsbManager;
    private String mProductName;
    private int mBaudRate;
    private int mDataBits;
    private int mStopBits;
    private int mParity;
    private int mWriteTimeout;
    private int mReadTimeout;

    private UsbSerialPort mPort;

    public SerialConnection(Context context, String productName, int baudRate, int dataBits,
                            int stopBits, int parity, int writeTimeout, int readTimeout) {
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mProductName = productName;
        mBaudRate = baudRate;
        mDataBits = dataBits;
        mStopBits = stopBits;
        mParity = parity;
        mWriteTimeout = writeTimeout;
        mReadTimeout = readTimeout;
    }

    public boolean initSerialPort() throws IOException {
        UsbSerialDriver usbSerialDriver = getSerialDriver();
        if (usbSerialDriver != null) {
            UsbSerialPort port = usbSerialDriver.getPorts().get(0);
            UsbDeviceConnection usbConnection = getUsbConnection(usbSerialDriver);
            if (usbConnection != null) {
                port.open(usbConnection);
                port.setParameters(mBaudRate, mDataBits, mStopBits, mParity);
                mPort = port;
            } else {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private UsbSerialDriver getSerialDriver() {
        List<UsbSerialDriver> availableDrivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            return null;
        }
        for (UsbSerialDriver availableDriver : availableDrivers) {
            if (Objects.equals(availableDriver.getDevice().getProductName(), mProductName)) {
                return availableDriver;
            }
        }
        return null;
    }

    private UsbDeviceConnection getUsbConnection(UsbSerialDriver usbSerialDriver) {
        UsbDevice device = usbSerialDriver.getDevice();
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        if (connection == null) {
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(device, pi);
            return null;
        }
        return connection;
    }

    public void closeSerialPort() {
        try {
            if (mPort != null) {
                mPort.close();
            }
            mPort = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toHexString(byte[] src) {
        return toHexString(src, src.length);
    }

    private String toHexString(byte[] src, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String s = Integer.toHexString(src[i] & 0xFF);
            if (s.length() < 2) {
                builder.append(0);
            }
            builder.append(s).append(' ');
        }
        return builder.toString().toUpperCase().trim();
    }

    public int write(byte[] outputArray) {
        int numBytesWrite = 0;
        try {
            if (mPort != null) {
                numBytesWrite = mPort.write(outputArray, mWriteTimeout);
                Log.i(TAG, "Write " + numBytesWrite + " bytes.");
                Log.i(TAG, "Write " + toHexString(outputArray));
            } else {
                Log.i(TAG, "mPort null");
            }
        } catch (IOException e) {
            closeSerialPort();
            e.printStackTrace();
        }
        return numBytesWrite;
    }

    public int read(byte[] inputArray) {
        int numBytesRead = 0;
        try {
            if (mPort != null) {
                numBytesRead = mPort.read(inputArray, mReadTimeout);
                Log.i(TAG, "Read " + numBytesRead + " bytes.");
                Log.i(TAG, "Read: " + toHexString(inputArray, numBytesRead));
            } else {
                Log.i(TAG, "mPort null");
            }
        } catch (IOException e) {
            closeSerialPort();
            e.printStackTrace();
        }
        return numBytesRead;
    }

    public boolean isInitiated() {
        return mPort != null;
    }
}
