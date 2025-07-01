Application.bind("smtp.forward", function (id) {
    DataSet.post(id + "/forward", {}, function (json) {
        Application.showAlert("SMTP", json.message, json.success);
    }, {dataType: "json"});
});

Application.bind("smtp.download", function (id) {
    DataSet.get(id + "/download", {}, function (json) {
        Application.showAlert("SMTP", json.message, json.success);
    }, {dataType: "json"});
});