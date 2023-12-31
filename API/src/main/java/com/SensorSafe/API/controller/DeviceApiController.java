package com.SensorSafe.API.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.SensorSafe.API.exceptions.DeviceNotFoundException;
import com.SensorSafe.API.exceptions.InvalidPermissionsException;
import com.SensorSafe.API.exceptions.RoomNotFoundException;
import com.SensorSafe.API.exceptions.UserNotFoundException;
import com.SensorSafe.API.midleware.MiddlewareHandler;
import com.SensorSafe.API.tokens.JwtRequest;
import com.SensorSafe.API.tokens.JwtResponse;
import com.SensorSafe.API.tokens.JwtUserDetailsService;
import com.SensorSafe.API.tokens.JwtTokenUtil;

import com.SensorSafe.API.auth.AuthHandler;
import com.SensorSafe.API.model.room.*;
import com.SensorSafe.API.model.Response;
import com.SensorSafe.API.model.device.*;
import com.SensorSafe.API.model.report.Report;
import com.SensorSafe.API.model.report.ReportType;
import com.SensorSafe.API.services.*;


@RestController
@RequestMapping("/api")
@Api(value = "Devices API", description = "Operations pertaining to devices", tags = "Devices")
public class DeviceApiController {

    
    private final UserService userService;
    private final RoomService roomService;
    private final ReportService reportService;
    private final ReportSensorService reportSensorService;
    private final DeviceService deviceService;
    private final SensorService sensorService;
    private final SensorEventService sensorEventService;
    private final AvailableDeviceService availableDeviceService;

    // Auth
    private final AuthHandler authHandler;

    private final MiddlewareHandler middlewareHandler;

    @Autowired
    public DeviceApiController(UserService userService, RoomService roomService, ReportService reportService, MiddlewareHandler middlewareHandler, ReportSensorService reportSensorService, DeviceService deviceService, SensorService sensorService, SensorEventService sensorEventService, AvailableDeviceService availableDeviceService, AuthHandler authHandler) {
        this.userService = userService;
        this.roomService = roomService;
        this.reportService = reportService;
        this.reportSensorService = reportSensorService;
        this.deviceService = deviceService;
        this.sensorService = sensorService;
        this.sensorEventService = sensorEventService;
        this.availableDeviceService = availableDeviceService;
        this.authHandler = authHandler;
        this.middlewareHandler = middlewareHandler;
    }

    @ApiOperation(value = "Get all devices", response = Iterable.class)
    @GetMapping("/devices")
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices().stream()
                .filter(device -> device.getRoomID() != null)
                .filter(device -> { 
                    Room room = roomService.getRoom(device.getRoomID());
                    if (room == null) {
                        return false;
                    }
                    return room.getUsers().contains(authHandler.getUsername());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @ApiOperation(value = "Create Sensor", response = Device.class)
    @PostMapping("/devices/sensor/create")
    public Response CreateSensor(@RequestBody Sensor sensor){

        sensorService.registerSensor(sensor);

        Report report = Report.builder()
                .reportId(null)
                .name(authHandler.getUsername())
                .type(ReportType.DEVICES)
                .date(new java.util.Date())
                .description("Device" + sensor.getName()+ " was created by" + authHandler.getUsername())
                .build();

        reportService.saveReport(report);

        return new Response("Sensor created successfully");

    }

    @ApiOperation(value = "Create Device", response = Device.class)
    @PostMapping("/devices/device/create")
    public Response CreateDevice(@RequestBody Device device){

        deviceService.registerDevice(device);

        Report report = Report.builder()
                .reportId(null)
                .name(authHandler.getUsername())
                .type(ReportType.DEVICES)
                .date(new java.util.Date())
                .description("Device" + device.getName()+ " was created by" + authHandler.getUsername())
                .build();

        reportService.saveReport(report);

        return new Response("Device created successfully");

    }


    @ApiOperation(value = "Get all sensors", response = Iterable.class)
    @GetMapping("/devices/sensors")
    public List<Sensor> getAllSensors() {
        return sensorService.getAllSensors().stream()
                .filter(sensor -> sensor.getRoomID() != null)
                .filter(sensor -> { 
                    Room room = roomService.getRoom(sensor.getRoomID());
                    if (room == null) {
                        return false;
                    }
                    return room.getUsers().contains(authHandler.getUsername());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @ApiOperation(value = "Get sensors by room", response = Iterable.class)
    @GetMapping("/devices/sensors/{roomId}")
    public List<Sensor> getSensorsByRoom(@PathVariable ObjectId roomId) {
        if (!roomService.exists(roomId)) {
            throw new RoomNotFoundException("Room not found");
        }

        Room room = roomService.getRoom(roomId);

        if (!room.getUsers().contains(authHandler.getUsername())) {
            throw new InvalidPermissionsException("Invalid Permissions");
        }

        return sensorService.getAllSensors().stream()
                .filter(sensor -> sensor.getRoomID() != null)
                .filter(sensor -> sensor.getRoomID().equals(roomId))
                .collect(java.util.stream.Collectors.toList());
    }

    @ApiOperation(value = "Get devices by room", response = Iterable.class)
    @GetMapping("/devices/{roomId}")
    public List<Device> getDevicesByRoom(@PathVariable ObjectId roomId) {
        if (!roomService.exists(roomId)) {
            throw new RoomNotFoundException("Room not found");
        }

        Room room = roomService.getRoom(roomId);

        if (!room.getUsers().contains(authHandler.getUsername())) {
            throw new InvalidPermissionsException("Invalid Permissions");
        }

        return deviceService.getAllDevices().stream()
                .filter(device -> device.getRoomID() != null)
                .filter(device -> device.getRoomID().equals(roomId))
                .collect(java.util.stream.Collectors.toList());
    }

    @ApiOperation(value = "Get sensor by id", response = Sensor.class)
    @GetMapping("/devices/sensors/id/{sensorId}")
    public Sensor getSensorById(@PathVariable ObjectId sensorId) {

        if (sensorId == null) {
            throw new DeviceNotFoundException("Please provide a sensor id");
        }

        if (!sensorService.sensorExists(sensorId)) {
            throw new DeviceNotFoundException("Unable to found sensor id");
        }

        Sensor sensor = sensorService.getSensorById(sensorId);

        if (sensor.getRoomID() == null || !roomService.exists(sensor.getRoomID())) {
            throw new RoomNotFoundException("Unable to found this room id: " + sensor.getRoomID());
        }

        Room room = roomService.getRoom(sensor.getRoomID());

        if (!room.getUsers().contains(authHandler.getUsername())) {
            throw new InvalidPermissionsException("Invalid Permissions");
        }

        return sensor;
    }



    @ApiOperation(value = "Get available devices", response = Iterable.class)
    @GetMapping("/devices/available/{deviceId}")
    public Response removeAvailableDevice(@PathVariable ObjectId deviceId) {
        if (!availableDeviceService.availableDeviceExists(deviceId)) {
            throw new DeviceNotFoundException("Device not found");
        }

        availableDeviceService.deleteAvailableDeviceById(deviceId);

        return new Response("Device available removed successfully");
    }


    @ApiOperation(value = "Get all available devices by username", response = Iterable.class)
    @GetMapping("/devices/available")
    public List<AvailableDevice> getAllAvailableDevices() {
        return availableDeviceService.getAllAvailableDevicesByUsername(authHandler.getUsername());
    }

    @DeleteMapping("/devices/available/{deviceId}")
    @ApiOperation(value = "Delete Device", notes = "Delete a Device by ID")
    public Response deleteByDeviceId(@PathVariable ObjectId deviceId) {

        if(availableDeviceService.availableDeviceExists(deviceId)){
            availableDeviceService.deleteAvailableDeviceById(deviceId);
            deviceService.deleteByDeviceId(deviceId);
            return new Response("Device successfully removed.");
        }
        

        Device device = deviceService.getDeviceById(deviceId);
        Room room = roomService.getRoom(device.getRoomID());
        
        room.getDevices().removeIf(dev -> dev.getDeviceId().equals(deviceId));
        roomService.updateRoom(room);

        deviceService.deleteByDeviceId(deviceId);
        
        return new Response("Device successfully removed.");
    }

    @PostMapping("/devices/add-accessible-to-room/{roomId}")
    @ApiOperation(value = "Add accessible device to room", notes = "Add accessible device to room")
    public Response addAccessibleDeviceToRoom(@PathVariable ObjectId roomId, @RequestBody Device device) {
        if (!roomService.exists(roomId)) {
            throw new RoomNotFoundException("Room not found");
        }

        Room room = roomService.getRoom(roomId);

        if (!room.getUsers().contains(authHandler.getUsername())) {
            throw new InvalidPermissionsException("Invalid Permissions");
        }

        if (!availableDeviceService.availableDeviceExists(device.getDeviceId())) {
            throw new DeviceNotFoundException("Device is not available");
        }

        Device deviceToBeAdded = new Device(device.getDeviceId(), device.getName(), device.getCategory(), roomId);

        deviceService.registerDevice(deviceToBeAdded);
        availableDeviceService.deleteAvailableDeviceById(device.getDeviceId());

        room.getDevices().add(deviceToBeAdded);
        roomService.updateRoom(room);


        return new Response("Device successfully added to room");
    }

    @PostMapping("/devices/sensors/add-accessible-to-room/{roomId}")
    @ApiOperation(value = "Add accessible sensor to room", notes = "Add accessible sensor to room")
    public Response addAccessibleSensorToRoom(@PathVariable ObjectId roomId, @RequestBody Sensor sensor) {
        if (!roomService.exists(roomId)) {
            throw new RoomNotFoundException("Room not found");
        }

        Room room = roomService.getRoom(roomId);

        if (!room.getUsers().contains(authHandler.getUsername())) {
            throw new InvalidPermissionsException("Invalid Permissions");
        }

        if (!availableDeviceService.availableDeviceExists(sensor.getDeviceId())) {
            throw new DeviceNotFoundException("Sensor is not available");
        }

        Sensor sensorToBeAdded = new Sensor(sensor.getDeviceId(), sensor.getName(), sensor.getCategory(), roomId, true, 0);

        sensorService.registerSensor(sensorToBeAdded);

        availableDeviceService.deleteAvailableDeviceById(sensor.getDeviceId());
        deviceService.deleteByDeviceId(sensor.getDeviceId());

        room.getDevices().add(sensorToBeAdded);
        roomService.updateRoom(room);

        return new Response("Sensor successfully added to room");
    }


    @PutMapping("/devices/device/edit/{deviceId}")
    @ApiOperation(value = "Edit a device", notes = "Edit a device")
    public Response editDevice(@PathVariable ObjectId deviceId, @RequestBody Device updatedDevice) {
        Device device = deviceService.getDeviceById(deviceId);
        AvailableDevice availableDevice = availableDeviceService.getAvailableDeviceById(deviceId);

        
        if (device == null) {
            // Se o dispositivo não for encontrado, pode retornar uma resposta de dispositivo não encontrado
            return new Response("Device not found");
        }

        // Atualize os campos que deseja alterar
        device.setName(updatedDevice.getName());
        device.setCategory(updatedDevice.getCategory());

        // Salve as alterações
        deviceService.updateDevice(device);

        if(availableDevice != null){
            availableDevice.setName(updatedDevice.getName());
            availableDevice.setCategory(updatedDevice.getCategory());
            availableDeviceService.updateAvailableDevice(availableDevice);
        }


        return new Response("Device updated successfully");
    }

    @PutMapping("/devices/device/remove-room/{deviceId}")
    @ApiOperation(value = "Remove room ID from a device", notes = "Remove room ID from a device")
    public Response removeRoomFromDevice(@PathVariable ObjectId deviceId) {
        Device device = deviceService.getDeviceById(deviceId);

        if (device == null) {
            return new Response("Device not found");
        }

        Room room = roomService.getRoom(device.getRoomID());
        room.getDevices().removeIf(dev -> dev.getDeviceId().equals(deviceId));
        roomService.updateRoom(room);

        device.setRoomID(null);

        deviceService.updateDevice(device);
        Device device1 = deviceService.getDeviceById(deviceId);
        deviceService.deleteByDeviceId(deviceId);
        deviceService.registerDevice(device1);

        
        

        

        return new Response("Room ID removed from the device successfully");
    }





}

