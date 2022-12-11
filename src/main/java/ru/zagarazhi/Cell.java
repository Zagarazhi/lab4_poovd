package ru.zagarazhi;

/**
 * Класс ПЗС ячейки
 */
public class Cell {

    /** Максимально возможное число тактов */
    public static final int MAX_TICK_COUNT = 20;
    /** Минимально возможное число тактов */
    public static final int MIN_TICK_COUNT = 1;
    /** Максимально возможный общий смаз */
    public static final double MAX_TOTAL_GREASE = 5d;
    /** Минимально возможный общий смаз */
    public static final double MIN_TOTAL_GREASE = -5d;
    /** Число лучей в ячейке */
    public static final int BEAMS_PER_CELL = 8;
    /** Расстояние между двумя соседними ячейками, где 1 - размер ячейки */
    public static final double DELAY = 1d / BEAMS_PER_CELL;
    /** Максимальное число лучей, которое необходимо для рассчетов */
    public static final int MAX_BEAM_COUNT = (int)Math.ceil((2 + MAX_TOTAL_GREASE) * BEAMS_PER_CELL) + 1;

    /** Число тактов в текущей конфигурации ячейки */
    private int tickCount;
    /** Индекс главного луча, то есть луча, который начинает с позиции 0 */
    private int mainBeamIndex;
    /** Индекс луча, на котором закончатся вычисления */
    private int lastBeamIndex;
    /** Величина смаза за все такты */
    private double totalGrease;
    /** Положения лучей */
    private double[] beamPositions;
    /** Сумма времени, в течение которого лучи освещали ячейку (в тактах) */
    private double[] sumByBeans;
    /** Время, в течение которого лучи освещали ячейку по тактам (в тактах) */
    private double[][] lightByTick;

    /**
     * Конструктор класса Ячейка
     * @param tickCount Число тактов
     * @param totalGrease Суммарный смаз
     */
    public Cell(int tickCount, double totalGrease) {
        this.tickCount = checkTickCount(tickCount);
        this.totalGrease = checkTotalGrease(totalGrease);
        this.beamPositions = new double[MAX_BEAM_COUNT];
        this.sumByBeans = new double[MAX_BEAM_COUNT];
        this.lightByTick = new double[MAX_TICK_COUNT][MAX_BEAM_COUNT];
        setMainAndLastBeams();
        resetBeamPositionsAndSum();
        countLight();
    }

    /**
     * Метод, устанавливающий число тактов
     * @param tickCount Новое значение числа тактов
     */
    public void setTickCount(int tickCount) {
        this.tickCount = checkTickCount(tickCount);
        setMainAndLastBeams();
        resetBeamPositionsAndSum();
        reserLightByTick();
        countLight();
    }

    /**
     * Метод, устанавливающий суммарный смаз
     * @param totalGrease Новое значение суммарного смаза
     */
    public void setTotalGrease(double totalGrease) {
        this.totalGrease = checkTotalGrease(totalGrease);
        setMainAndLastBeams();
        resetBeamPositionsAndSum();
        reserLightByTick();
        countLight();
    }

    /**
     * Метод, возвращающий индекс главного луча
     * @return Индекс главного луча
     */
    public int getMainBeamIndex() {
        return mainBeamIndex;
    }

    /**
     * Метод, возвращающий индекс последнего луча
     * @return Индекс последнего луча
     */
    public int getLastBeamIndex() {
        return lastBeamIndex;
    }

    /**
     * Метод, возвращающий максимальное суммарное значение света
     * @return Максимальное суммарное значение света
     */
    public double getMaxLight() {
        double max = 0;
        for(int i = 0; i < lastBeamIndex; i++) {
            if(max < sumByBeans[i]) max = sumByBeans[i];
        }
        return max;
    }

    /**
     * Метод, возвращающий значение света по индексам такта и луча
     * @param tickIndex Индекс такта
     * @param beamIndex Индекс луча
     * @return Значение света
     */
    public double getLightByTickAndBeam(int tickIndex, int beamIndex) {
        return lightByTick[tickIndex][beamIndex];
    }

    /**
     * Метод, возвращающий значение суммарного света по индексу луча
     * @param beamIndex Индекс луча
     * @return Суммарное значение света
     */
    public double getSumByBeans(int beamIndex) {
        return sumByBeans[beamIndex];
    }

    /**
     * Метод, расчитывающий значение света по тактам и лучам, а также суммарный свет по лучам
     */
    private void countLight() {
        double r, l; //Правая и левая границы отрезка пересечения 
        double delta; //Дополнительное время, которое луч светил на ячейку
        double movement = 1 + totalGrease / tickCount; //Перемещение луча за один такт
        //Цикл по всем необходимым для вычислений тактам
        for(int position = 0; position < tickCount; position++) {
            //Цикл по всем необходимым для вычислений лучам
            for(int beam = 0; beam <= lastBeamIndex; beam++) {
                //Опредление левой границы
                l = beamPositions[beam] > position ? beamPositions[beam] : position;
                //Определение правой границы
                r = beamPositions[beam] + movement < position + 1 ? beamPositions[beam] + movement : position + 1;
                //Опредление дополнительного времени
                delta = (r - l) / movement;
                //Если отрезок положительный - добавить время
                //Если отрезок отрицательный - пересечения нет
                if(delta > 0){
                    lightByTick[position][beam] += delta;
                    sumByBeans[beam] += delta;
                }
                //Перемещение лучаы
                beamPositions[beam] += movement;
            }
        }
    }

    /**
     * Метод, устанавливающий индексы главного луча и последнего луча 
     */
    private void setMainAndLastBeams() {
        if(totalGrease < 0) {
            double movement = 1 + totalGrease / tickCount;
            this.mainBeamIndex = (int)Math.ceil((movement) * BEAMS_PER_CELL);
            this.lastBeamIndex = (int)Math.ceil((movement + movement - totalGrease) * BEAMS_PER_CELL) + 1;
            return;
        }
        this.mainBeamIndex = (int)Math.ceil((1 + totalGrease) * BEAMS_PER_CELL);
        this.lastBeamIndex = (int)Math.ceil((2 + totalGrease) * BEAMS_PER_CELL);
    }

    /**
     * Метод, сбрасывающий положения лучей и значения сумм при перерасчете
     */
    private void resetBeamPositionsAndSum(){
        double startPosition = -mainBeamIndex * DELAY;
        for(int i = 0; i <= lastBeamIndex; i++) {
            beamPositions[i] = startPosition;
            startPosition += DELAY;
            sumByBeans[i] = 0;
        }
    }

    /**
     * Метод, сбрасывающий значение света по тактам и лучам
     */
    private void reserLightByTick() {
        for(int tick = 0; tick < tickCount; tick++) {
            for(int beam = 0; beam <= lastBeamIndex; beam++) {
                lightByTick[tick][beam] = 0;
            }
        }
    }

    /**
     * Метод, проверяющий граничные случаи для числа тактов
     * @param tickCount Проверяемое число тактов
     * @return Проверенное число тактов
     */
    private int checkTickCount(int tickCount) {
        //Проверка ограничений, что число тактов не больше максимального числа тактов и не меньше минимального
        if(tickCount > MAX_TICK_COUNT) return MAX_TICK_COUNT;
        else if(tickCount < MIN_TICK_COUNT) return MIN_TICK_COUNT;
        return tickCount;
    }

    /**
     * Метод, проверяющий граничные случаи для суммарного смаза
     * @param totalGrease Проверяемый смаз
     * @return Проверенный смаз
     */
    private double checkTotalGrease(double totalGrease) {
        //Проверка ограничений, что суммарный смаз не больше максимального значения смаза и не меньше минимального
        if(totalGrease > MAX_TOTAL_GREASE) return MAX_TOTAL_GREASE;
        else if(totalGrease < MIN_TOTAL_GREASE) return MIN_TOTAL_GREASE;
        //Проверка огранечения, что отрицательное значение смаза по модулю не больше числа тактов
        //В противном случае скорость лучей становится отрицательной (по формуле 1 + (B / N)) 
        else if(tickCount < -totalGrease) return tickCount - 1;
        return totalGrease;
    }
}
