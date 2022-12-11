package ru.zagarazhi;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * JavaFX App
 */
public class App extends Application {

    /** Максимально возможное число тактов */
    public static final int MAX_TICK_COUNT = Cell.MAX_TICK_COUNT;
    /** Минимально возможное число тактов */
    public static final int MIN_TICK_COUNT = Cell.MIN_TICK_COUNT;
    /** Максимально возможный общий смаз */
    public static final double MAX_TOTAL_GREASE = Cell.MAX_TOTAL_GREASE;
    /** Минимально возможный общий смаз */
    public static final double MIN_TOTAL_GREASE = Cell.MIN_TOTAL_GREASE;
    /** Минимальное изменение общего смаза */
    public static final double ACCURANCY = 0.001;

    /** Сцена приложения */
    private static Scene scene;
    /** Число тактов в текущей конфигурации ячейки */
    private int tickCount = 6;

    /**
     * Метод обновления графиков
     * @param border Разметка сцены
     * @param cell Объект ячейки
     */
    private void updateChart(BorderPane border, Cell cell) {
        //Индекс главного луча (на графике будет считаться нулевым)
        int mainBeamIndex = cell.getMainBeamIndex();
        //Индекс последнего луча
        int lastBeamIndex = cell.getLastBeamIndex();
        
        //Описание оси X
        NumberAxis xAxis = new NumberAxis(0 - mainBeamIndex, lastBeamIndex - mainBeamIndex, 1); 
        xAxis.setLabel("Лучи");

        //Описание оси Y
        NumberAxis yAxis = new NumberAxis(0, (int)Math.ceil(cell.getMaxLight()) + 1, 1); 
        yAxis.setLabel("Такты");

        //Создание объекта графика
        LineChart<Number, Number> linechart = new LineChart<>(xAxis, yAxis);
        //Действие, отменяющее выделение установленных точек
        linechart.setCreateSymbols(false);
        
        //Цикл по тактам
        for(int i = 0; i < tickCount; i++) {
            //Создание новой серии данных для графика
            XYChart.Series<Number, Number> series = new XYChart.Series<>(); 
            //Установка имени серии 
            series.setName("Такт" + (i + 1));
            //Цикл по всем лучам
            for(int j = 0; j <= lastBeamIndex; j++) {
                //Создание пары [позиция - значение] дял данной серии
                series.getData().add(new XYChart.Data<Number, Number>(j - mainBeamIndex, cell.getLightByTickAndBeam(i, j)));
            }
            //Добавление серии на график
            linechart.getData().add(series); 
        }

        //Если число тактов больше одного - добавляется сумма по тактам
        if(tickCount > 1){
            //Создание новой серии данных для графика
            XYChart.Series<Number, Number> series = new XYChart.Series<>(); 
            //Установка имени серии 
            series.setName("За все такты");
            //Цикл по всем лучам
            for(int i = 0; i <= lastBeamIndex; i++) {
                //Создание пары [позиция - значение] дял данной серии
                series.getData().add(new XYChart.Data<Number, Number>(i - mainBeamIndex, cell.getSumByBeans(i)));
            }
            //Добавление серии на график
            linechart.getData().add(series); 
        }
        
        //Добавление графика в разметку
        border.setCenter(linechart);
    }

    @Override
    public void start(Stage stage) throws IOException {
        //Установка формата десятичного числа
        DecimalFormat df = new DecimalFormat("0.000");
        //Создание объекта ячейки
        Cell cell = new Cell(6, 0);

        //Разметка типа BorderPane
        BorderPane border = new BorderPane();
        //Горизонтальная разметка
        HBox controlsHBox = new HBox();
        //Создание окна
        scene = new Scene(border, 640, 480);
        stage.setScene(scene);
        stage.show();

        //Создание элементов управления
        Label tickLabelName = new Label("Число таков:");
        Label tickLabelCount = new Label(Integer.toString(tickCount));
        Label totalGreaseLabelName = new Label("Смаз:");
        Label totalGreaseCount = new Label("0.000");
        Slider tickSlider = new Slider(MIN_TICK_COUNT, MAX_TICK_COUNT, tickCount);
        Slider totalGreaseSlider = new Slider(MIN_TOTAL_GREASE, MAX_TOTAL_GREASE, 0);

        //Настройка элементов управления
        tickSlider.setShowTickLabels(false);
        tickSlider.setBlockIncrement(1);
        totalGreaseSlider.setShowTickLabels(false);
        totalGreaseSlider.setBlockIncrement(ACCURANCY);

        //Добавление события изменения слайдера числа тактов
        tickSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                tickCount = newValue.intValue();
                //Измнение минимального значения общего смаза в зависимости отч числа тактов
                if(tickCount <= 5) totalGreaseSlider.setMin(-tickCount + ACCURANCY);
                else totalGreaseSlider.setMin(MIN_TOTAL_GREASE);
                //Отображение нового числа тактов
                tickLabelCount.setText(Integer.toString(tickCount));
                //Обновление ячейки
                cell.setTickCount(tickCount);
                //Обновление графика
                updateChart(border, cell);
            }
        });

        //Добавление события изменения слайдера смаза
        totalGreaseSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //Новое значение смаза
                double totalGrease = newValue.doubleValue();
                //Отображение нового значения смаза в соответсвии с форматом десятичного числа
                totalGreaseCount.setText(df.format(totalGrease));
                //Обновление ячейки
                cell.setTotalGrease(totalGrease);
                //Обновление графика
                updateChart(border, cell);
            }
        });

        //Добавление элементов на горизонтальную разметку
        controlsHBox.setPadding(new Insets(10, 10, 10, 10));
        controlsHBox.setAlignment(Pos.BASELINE_CENTER);
        controlsHBox.setSpacing(10);
        controlsHBox.getChildren().addAll(tickLabelName, 
                                            tickSlider, 
                                            tickLabelCount, 
                                            totalGreaseLabelName, 
                                            totalGreaseSlider, 
                                            totalGreaseCount);

        //Уставнока горизонтальной разметки на основной разметке
        border.setBottom(controlsHBox);
        //Первая отрисовка графика
        updateChart(border, cell);
    }

    public static void main(String[] args) {
        launch();
    }

}