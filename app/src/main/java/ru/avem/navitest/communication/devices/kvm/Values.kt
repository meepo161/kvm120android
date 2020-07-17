package ru.avem.navitest.communication.devices.kvm

class Values(val isResponding: Boolean,
             val voltageAmp: Float = Float.NaN,
             val voltageAvr: Float = Float.NaN,
             val voltageRms: Float = Float.NaN,
             val frequency: Float = Float.NaN,
             val razmah: Float = Float.NaN,
             val chtoEshe: Float = Float.NaN,
             val coefficentAmp: Float = Float.NaN,
             val coefficentForm: Float = Float.NaN,
             val timeAveraging: Float = Float.NaN)