Application.bind("rest.simulation.run", function () {
    Logger.info("Start simulation with id '" + DataSet.getId() + "'");
    DataSet.post("schedule/" + DataSet.getId(), {}, function (json) {
        Application.showInfoAlert("Simulation", json.message)
    }, {dataType: "json"});

});

Application.bind("rest.simulation.design", function (id) {

});