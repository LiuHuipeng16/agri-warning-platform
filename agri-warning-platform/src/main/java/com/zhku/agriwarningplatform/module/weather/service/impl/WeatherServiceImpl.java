package com.zhku.agriwarningplatform.module.weather.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhku.agriwarningplatform.common.errorcode.WeatherErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.module.weather.service.WeatherService;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherForecastDTO;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherTodayDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 天气 Service 实现类
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    /**
     * 湛江固定经纬度
     */
    private static final double ZHANJIANG_LATITUDE = 21.2707D;
    private static final double ZHANJIANG_LONGITUDE = 110.3594D;

    /**
     * 固定城市名称
     */
    private static final String CITY_NAME = "湛江";

    /**
     * Open-Meteo 预报接口地址
     */
    private static final String OPEN_METEO_FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    /**
     * HttpClient
     */
    private final HttpClient httpClient;

    /**
     * ObjectMapper
     */
    private final ObjectMapper objectMapper;
    /**
     * 默认预报天数
     */
    private static final int DEFAULT_FORECAST_DAYS = 4;

    public WeatherServiceImpl() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public WeatherTodayDTO getTodayWeather() {
        try {
            LocalDate today = LocalDate.now();

            String url = buildTodayWeatherUrl(today);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("调用 Open-Meteo 接口失败，statusCode={}, body={}", response.statusCode(), response.body());
                throw new ServiceException(WeatherErrorCode.WEATHER_QUERY_FAILED);
            }

            OpenMeteoWeatherResponse responseObj =
                    objectMapper.readValue(response.body(), OpenMeteoWeatherResponse.class);

            if (Objects.isNull(responseObj)
                    || Objects.isNull(responseObj.getHourly())
                    || Objects.isNull(responseObj.getHourly().getTime())
                    || responseObj.getHourly().getTime().isEmpty()) {
                throw new ServiceException(WeatherErrorCode.WEATHER_DATA_EMPTY);
            }

            return buildTodayWeatherDTO(responseObj, today);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取湛江当天天气异常", e);
            throw new ServiceException(WeatherErrorCode.WEATHER_QUERY_FAILED);
        }
    }

    @Override
    public List<WeatherForecastDTO> getForecastWeather(Integer days) {
        try {
            int validDays = (days == null ? DEFAULT_FORECAST_DAYS : days);
            if (validDays < 1 || validDays > 7) {
                throw new ServiceException(WeatherErrorCode.FORECAST_DAYS_INVALID);
            }

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(validDays);

            String url = buildForecastWeatherUrl(startDate, endDate);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("调用 Open-Meteo 多天天气接口失败，statusCode={}, body={}", response.statusCode(), response.body());
                throw new ServiceException(WeatherErrorCode.WEATHER_QUERY_FAILED);
            }

            OpenMeteoWeatherResponse responseObj =
                    objectMapper.readValue(response.body(), OpenMeteoWeatherResponse.class);

            if (Objects.isNull(responseObj)
                    || Objects.isNull(responseObj.getHourly())
                    || Objects.isNull(responseObj.getHourly().getTime())
                    || responseObj.getHourly().getTime().isEmpty()) {
                throw new ServiceException(WeatherErrorCode.WEATHER_DATA_EMPTY);
            }

            return buildForecastWeatherDTOList(responseObj, startDate, endDate);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取湛江多天天气预报异常，days={}", days, e);
            throw new ServiceException(WeatherErrorCode.WEATHER_QUERY_FAILED);
        }
    }

    /**
     * 构建获取当天天气的 Open-Meteo URL
     *
     * @param today 今天日期
     * @return 请求URL
     */
    private String buildTodayWeatherUrl(LocalDate today) {
        return OPEN_METEO_FORECAST_URL
                + "?latitude=" + ZHANJIANG_LATITUDE
                + "&longitude=" + ZHANJIANG_LONGITUDE
                + "&timezone=auto"
                + "&start_date=" + today
                + "&end_date=" + today
                + "&hourly=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,weather_code";
    }

    /**
     * 组装当天天气 DTO
     *
     * 规则：
     * 1. current 数据：取当前时间最近的一条小时级数据
     * 2. min/max：当天所有小时温度中的最小值和最大值
     * 3. precipitation：当天所有小时降雨量累计
     *
     * @param responseObj Open-Meteo 响应
     * @param today       今天日期
     * @return 天气DTO
     */
    private WeatherTodayDTO buildTodayWeatherDTO(OpenMeteoWeatherResponse responseObj, LocalDate today) {
        OpenMeteoHourly hourly = responseObj.getHourly();

        List<String> timeList = hourly.getTime();
        List<BigDecimal> temperatureList = hourly.getTemperature2m();
        List<BigDecimal> humidityList = hourly.getRelativeHumidity2m();
        List<BigDecimal> precipitationList = hourly.getPrecipitation();
        List<BigDecimal> windSpeedList = hourly.getWindSpeed10m();
        List<Integer> weatherCodeList = hourly.getWeatherCode();

        if (timeList == null || timeList.isEmpty()) {
            throw new ServiceException(WeatherErrorCode.WEATHER_DATA_EMPTY);
        }

        int nearestIndex = findNearestHourIndex(timeList);

        BigDecimal currentTemperature = getSafeDecimalValue(temperatureList, nearestIndex);
        BigDecimal currentHumidity = getSafeDecimalValue(humidityList, nearestIndex);
        BigDecimal currentWindSpeed = getSafeDecimalValue(windSpeedList, nearestIndex);
        Integer currentWeatherCode = getSafeIntegerValue(weatherCodeList, nearestIndex);

        BigDecimal tempMin = null;
        BigDecimal tempMax = null;
        BigDecimal totalPrecipitation = BigDecimal.ZERO;

        if (temperatureList != null) {
            for (BigDecimal temperature : temperatureList) {
                if (temperature == null) {
                    continue;
                }
                if (tempMin == null || temperature.compareTo(tempMin) < 0) {
                    tempMin = temperature;
                }
                if (tempMax == null || temperature.compareTo(tempMax) > 0) {
                    tempMax = temperature;
                }
            }
        }

        if (precipitationList != null) {
            for (BigDecimal precipitation : precipitationList) {
                if (precipitation != null) {
                    totalPrecipitation = totalPrecipitation.add(precipitation);
                }
            }
        }

        WeatherTodayDTO dto = new WeatherTodayDTO();
        dto.setCity(CITY_NAME);
        dto.setDate(today.toString());
        dto.setTemperature(scale(currentTemperature));
        dto.setTempMin(scale(tempMin));
        dto.setTempMax(scale(tempMax));
        dto.setHumidity(scale(currentHumidity));
        dto.setPrecipitation(scale(totalPrecipitation));
        dto.setWindSpeed(scale(currentWindSpeed));
        dto.setWeatherDesc(convertWeatherCodeToDesc(currentWeatherCode));
        dto.setUpdateTime(buildUpdateTime(timeList, nearestIndex));

        return dto;
    }

    /**
     * 找到离当前时间最近的小时索引
     *
     * @param timeList 时间列表
     * @return 最近索引
     */
    private int findNearestHourIndex(List<String> timeList) {
        if (timeList == null || timeList.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        int nearestIndex = 0;
        long minDiff = Long.MAX_VALUE;

        for (int i = 0; i < timeList.size(); i++) {
            String time = timeList.get(i);
            if (time == null || time.isBlank()) {
                continue;
            }

            try {
                LocalDateTime timePoint = LocalDateTime.parse(time);
                long diff = Math.abs(java.time.Duration.between(now, timePoint).toMinutes());
                if (diff < minDiff) {
                    minDiff = diff;
                    nearestIndex = i;
                }
            } catch (Exception e) {
                log.warn("解析 Open-Meteo 时间异常，time={}", time, e);
            }
        }

        return nearestIndex;
    }

    /**
     * 安全获取 BigDecimal 列表指定位置的值
     *
     * @param list  列表
     * @param index 索引
     * @return 值
     */
    private BigDecimal getSafeDecimalValue(List<BigDecimal> list, int index) {
        if (list == null || list.isEmpty() || index < 0 || index >= list.size()) {
            return BigDecimal.ZERO;
        }
        return list.get(index) == null ? BigDecimal.ZERO : list.get(index);
    }

    /**
     * 安全获取 Integer 列表指定位置的值
     *
     * @param list  列表
     * @param index 索引
     * @return 值
     */
    private Integer getSafeIntegerValue(List<Integer> list, int index) {
        if (list == null || list.isEmpty() || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 数值统一保留两位小数
     *
     * @param value 原值
     * @return 处理后值
     */
    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 构建更新时间
     *
     * @param timeList 时间列表
     * @param index    最近索引
     * @return 更新时间字符串
     */
    private String buildUpdateTime(List<String> timeList, int index) {
        if (timeList == null || timeList.isEmpty() || index < 0 || index >= timeList.size()) {
            return "";
        }
        String time = timeList.get(index);
        if (time == null || time.isBlank()) {
            return "";
        }
        return time.replace("T", " ");
    }

    /**
     * Open-Meteo weather_code 转中文天气描述
     *
     * @param weatherCode 天气码
     * @return 天气描述
     */
    private String convertWeatherCodeToDesc(Integer weatherCode) {
        if (weatherCode == null) {
            return "";
        }
        switch (weatherCode) {
            case 0:
                return "晴";
            case 1:
            case 2:
            case 3:
                return "多云";
            case 45:
            case 48:
                return "雾";
            case 51:
            case 53:
            case 55:
                return "毛毛雨";
            case 61:
            case 63:
            case 65:
                return "小雨";
            case 66:
            case 67:
                return "冻雨";
            case 71:
            case 73:
            case 75:
                return "降雪";
            case 77:
                return "雪粒";
            case 80:
            case 81:
            case 82:
                return "阵雨";
            case 85:
            case 86:
                return "阵雪";
            case 95:
                return "雷暴";
            case 96:
            case 99:
                return "雷暴伴冰雹";
            default:
                return "未知";
        }
    }

    /**
     * Open-Meteo 响应对象
     */
    public static class OpenMeteoWeatherResponse {

        /**
         * 小时级数据
         */
        private OpenMeteoHourly hourly;

        public OpenMeteoHourly getHourly() {
            return hourly;
        }

        public void setHourly(OpenMeteoHourly hourly) {
            this.hourly = hourly;
        }
    }

    /**
     * Open-Meteo 小时级天气数据
     */
    public static class OpenMeteoHourly {

        /**
         * 时间列表
         */
        private List<String> time;

        /**
         * 温度
         */
        @JsonProperty("temperature_2m")
        private List<BigDecimal> temperature2m;

        /**
         * 相对湿度
         */
        @JsonProperty("relative_humidity_2m")
        private List<BigDecimal> relativeHumidity2m;

        /**
         * 降雨量
         */
        private List<BigDecimal> precipitation;

        /**
         * 风速
         */
        @JsonProperty("wind_speed_10m")
        private List<BigDecimal> windSpeed10m;

        /**
         * 天气码
         */
        @JsonProperty("weather_code")
        private List<Integer> weatherCode;

        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public List<BigDecimal> getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(List<BigDecimal> temperature2m) {
            this.temperature2m = temperature2m;
        }

        public List<BigDecimal> getRelativeHumidity2m() {
            return relativeHumidity2m;
        }

        public void setRelativeHumidity2m(List<BigDecimal> relativeHumidity2m) {
            this.relativeHumidity2m = relativeHumidity2m;
        }

        public List<BigDecimal> getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(List<BigDecimal> precipitation) {
            this.precipitation = precipitation;
        }

        public List<BigDecimal> getWindSpeed10m() {
            return windSpeed10m;
        }

        public void setWindSpeed10m(List<BigDecimal> windSpeed10m) {
            this.windSpeed10m = windSpeed10m;
        }

        public List<Integer> getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(List<Integer> weatherCode) {
            this.weatherCode = weatherCode;
        }
    }
    /**
     * 构建获取多天天气预报的 Open-Meteo URL
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 请求URL
     */
    private String buildForecastWeatherUrl(LocalDate startDate, LocalDate endDate) {
        return OPEN_METEO_FORECAST_URL
                + "?latitude=" + ZHANJIANG_LATITUDE
                + "&longitude=" + ZHANJIANG_LONGITUDE
                + "&timezone=auto"
                + "&start_date=" + startDate
                + "&end_date=" + endDate
                + "&hourly=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,weather_code";
    }
    /**
     * 组装多天天气预报 DTO 列表
     *
     * @param responseObj Open-Meteo 响应
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return DTO列表
     */
    private List<WeatherForecastDTO> buildForecastWeatherDTOList(OpenMeteoWeatherResponse responseObj,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        List<WeatherForecastDTO> result = new java.util.ArrayList<>();

        OpenMeteoHourly hourly = responseObj.getHourly();

        List<String> timeList = hourly.getTime();
        List<BigDecimal> temperatureList = hourly.getTemperature2m();
        List<BigDecimal> humidityList = hourly.getRelativeHumidity2m();
        List<BigDecimal> precipitationList = hourly.getPrecipitation();
        List<BigDecimal> windSpeedList = hourly.getWindSpeed10m();
        List<Integer> weatherCodeList = hourly.getWeatherCode();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BigDecimal tempMin = null;
            BigDecimal tempMax = null;
            BigDecimal humiditySum = BigDecimal.ZERO;
            int humidityCount = 0;
            BigDecimal precipitationSum = BigDecimal.ZERO;
            BigDecimal maxWindSpeed = null;
            Integer middayWeatherCode = null;
            String updateTime = date + " 12:00:00";

            for (int i = 0; i < timeList.size(); i++) {
                String time = timeList.get(i);
                if (time == null || time.length() < 10) {
                    continue;
                }

                String dateStr = time.substring(0, 10);
                if (!date.toString().equals(dateStr)) {
                    continue;
                }

                BigDecimal temperature = getSafeDecimalValue(temperatureList, i);
                BigDecimal humidity = getSafeDecimalValue(humidityList, i);
                BigDecimal precipitation = getSafeDecimalValue(precipitationList, i);
                BigDecimal windSpeed = getSafeDecimalValue(windSpeedList, i);
                Integer weatherCode = getSafeIntegerValue(weatherCodeList, i);

                if (tempMin == null || temperature.compareTo(tempMin) < 0) {
                    tempMin = temperature;
                }
                if (tempMax == null || temperature.compareTo(tempMax) > 0) {
                    tempMax = temperature;
                }

                humiditySum = humiditySum.add(humidity);
                humidityCount++;

                precipitationSum = precipitationSum.add(precipitation);

                if (maxWindSpeed == null || windSpeed.compareTo(maxWindSpeed) > 0) {
                    maxWindSpeed = windSpeed;
                }

                if (time.endsWith("12:00")) {
                    middayWeatherCode = weatherCode;
                    updateTime = time.replace("T", " ");
                }
            }

            WeatherForecastDTO dto = new WeatherForecastDTO();
            dto.setCity(CITY_NAME);
            dto.setDate(date.toString());
            dto.setTempMin(scale(tempMin));
            dto.setTempMax(scale(tempMax));
            dto.setAvgHumidity(humidityCount == 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : humiditySum.divide(BigDecimal.valueOf(humidityCount), 2, RoundingMode.HALF_UP));
            dto.setPrecipitation(scale(precipitationSum));
            dto.setMaxWindSpeed(scale(maxWindSpeed));
            dto.setWeatherDesc(convertWeatherCodeToDesc(middayWeatherCode));
            dto.setUpdateTime(updateTime);

            result.add(dto);
        }

        return result;
    }
}