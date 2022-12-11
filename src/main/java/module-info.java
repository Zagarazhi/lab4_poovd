module ru.zagarazhi {
    requires javafx.controls;
    requires javafx.fxml;

    opens ru.zagarazhi to javafx.fxml;
    exports ru.zagarazhi;
}
