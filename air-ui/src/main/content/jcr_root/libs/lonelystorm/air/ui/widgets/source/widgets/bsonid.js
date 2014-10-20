/*

ObjectId is a 12-byte BSON type, constructed using:

  - a 4-byte value representing the seconds since the Unix epoch,
  - a 3-byte machine identifier,
  - a 2-byte process id, and
  - a 3-byte counter, starting with a random value.

*/

CQ.Ext.namespace('LonelyStorm.Air.Bson');

LonelyStorm.Air.Bson.ObjectId = (function() {

    var machineId = Math.floor(Math.random() * (16777215 + 1));
    var processId = Math.floor(Math.random() * (65535 + 1));
    var increment = Math.floor(Math.random() * (16777215 + 1));

    var cookie = parseInt(CQ.Ext.util.Cookies.get('ObjectId.machineId'), 10);
    if (cookie >= 0 && cookie <= 16777215) {
        machineId = cookie;
    }

    var expiry = new Date();
    expiry.setFullYear(expiry.getFullYear() + 20);
    CQ.Ext.util.Cookies.set("ObjectId.machineId", machineId, expiry, "/");

    function ObjectID() {
        this.timestamp = Math.floor(new Date().valueOf() / 1000).toString(16);
        this.machineId = machineId.toString(16);
        this.processId = processId.toString(16);
        this.increment = (increment++).toString(16);
        if (increment >= 16777215) {
            increment = 0;
        }
    }

    return ObjectID;
})();

LonelyStorm.Air.Bson.ObjectId.prototype.toString = function () {
    return '00000000'.substr(0, 8 - this.timestamp.length) + this.timestamp +
           '000000'.substr(0, 6 - this.machineId.length) + this.machineId +
           '0000'.substr(0, 4 - this.processId.length) + this.processId +
           '000000'.substr(0, 6 - this.increment.length) + this.increment;
};
