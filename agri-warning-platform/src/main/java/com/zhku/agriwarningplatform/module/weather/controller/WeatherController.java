package com.zhku.agriwarningplatform.module.weather.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 20:31
 */
import com.zhku.agriwarningplatform.common.errorcode.WeatherErrorCode;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.module.weather.controller.vo.WeatherForecastVO;
import com.zhku.agriwarningplatform.module.weather.controller.vo.WeatherTodayVO;
import com.zhku.agriwarningplatform.module.weather.service.WeatherService;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherForecastDTO;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherTodayDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * 天气 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * 获取湛江当天天气
     *
     * @return 当天天气信息
     */
    @GetMapping("/today")
    public CommonResult<WeatherTodayVO> getTodayWeather() {
        log.info("进入接口:WeatherController#getTodayWeather");
        WeatherTodayDTO dto = weatherService.getTodayWeather();
        WeatherTodayVO vo = convertToVO(dto);
        return CommonResult.success(vo);
    }


    /**
     * 获取湛江多天天气预报
     *
     * @param days 未来天数，默认4，范围1~7
     * @return 多天天气预报
     */
    @GetMapping("/forecast")
    public CommonResult<List<WeatherForecastVO>> getForecastWeather(
            @RequestParam(value = "days", required = false) Integer days) {
        log.info("进入接口:WeatherController#getForecastWeather,days");
        try {
            Integer validDays = days;
            if (Objects.isNull(validDays)) {
                validDays = 4;
            }

            if (validDays < 1 || validDays > 7) {
                throw new ControllerException(WeatherErrorCode.FORECAST_DAYS_INVALID);
            }

            List<WeatherForecastDTO> dtoList = weatherService.getForecastWeather(validDays);
            List<WeatherForecastVO> voList = convertToForecastVOList(dtoList);

            return CommonResult.success(voList);
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取湛江多天天气预报异常，days={}", days, e);
            throw new ControllerException(WeatherErrorCode.FORECAST_DAYS_INVALID);
        }
    }

    private WeatherTodayVO convertToVO(WeatherTodayDTO dto) {
        WeatherTodayVO vo = new WeatherTodayVO();
        vo.setCity(dto.getCity());
        vo.setDate(dto.getDate());
        vo.setTemperature(dto.getTemperature());
        vo.setTempMin(dto.getTempMin());
        vo.setTempMax(dto.getTempMax());
        vo.setHumidity(dto.getHumidity());
        vo.setPrecipitation(dto.getPrecipitation());
        vo.setWindSpeed(dto.getWindSpeed());
        vo.setWeatherDesc(dto.getWeatherDesc());
        vo.setUpdateTime(dto.getUpdateTime());
        return vo;
    }


    private List<WeatherForecastVO> convertToForecastVOList(List<WeatherForecastDTO> dtoList) {
        List<WeatherForecastVO> voList = new ArrayList<>();
        if (dtoList == null || dtoList.isEmpty()) {
            return voList;
        }

        for (WeatherForecastDTO dto : dtoList) {
            WeatherForecastVO vo = new WeatherForecastVO();
            vo.setCity(dto.getCity());
            vo.setDate(dto.getDate());
            vo.setTempMin(dto.getTempMin());
            vo.setTempMax(dto.getTempMax());
            vo.setAvgHumidity(dto.getAvgHumidity());
            vo.setPrecipitation(dto.getPrecipitation());
            vo.setMaxWindSpeed(dto.getMaxWindSpeed());
            vo.setWeatherDesc(dto.getWeatherDesc());
            vo.setUpdateTime(dto.getUpdateTime());
            voList.add(vo);
        }

        return voList;
    }
}
