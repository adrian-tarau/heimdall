Application.bind("rest.project.sync", function () {
    DataSet.post("sync", {}, function (json) {
        Application.showInfoAlert("Project", json.message)
    }, {dataType: "json"});
});

Application.bind("rest.library.history", function () {
    DataSet.get("view/history/" +  DataSet.getId(), {}, function (data) {
        Application.loadModal("history-modal", data);
    });
});

Application.bind("rest.library.history.view", function (id) {
    CodeEditor.loadModal("history/view/" +  id);
});

Application.bind("rest.library.history.revert", function (id) {
    DataSet.get("history/revert/" +  id, {}, function (data) {
        Application.showInfoAlert("Restore", data.message);
    }, {dataType: "json"});
});

Application.bind("rest.library.design", function () {
    CodeEditor.loadModal("design/" + DataSet.getId());
});

Application.bind("rest.result.view.logs", function (id) {
    DataSet.get("log/" + id, {}, function (data) {
        Application.loadModal("log-modal", data);
    });
});

Application.bind("rest.result.view.data", function (id) {
    DataSet.get("data/" + id, {}, function (data) {
        Application.loadModal("data-modal", data);
    });
});

Application.bind("rest.result.view.report", function (id) {
    DataSet.get("report/" + id, {}, function (data) {
        Application.loadModal("report-modal", data);
    });
});

Application.bind("rest.schedule.run", function () {
    Logger.info("Start simulation with id '" + DataSet.getId() + "'");
    DataSet.post("run/" + DataSet.getId(), {}, function (json) {
        Application.showInfoAlert("Simulation", json.message)
    }, {dataType: "json"});
});