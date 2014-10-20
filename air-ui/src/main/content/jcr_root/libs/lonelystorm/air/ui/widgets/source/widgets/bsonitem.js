CQ.Ext.namespace('LonelyStorm.Air.Bson');

LonelyStorm.Air.Bson.Item = CQ.Ext.extend(CQ.form.MultiField.Item, {

    bsonIdValue: null,

    bsonId: '',

    updateBsonId: function() {
        if (this.bsonId === '') {
            var id = new LonelyStorm.Air.Bson.ObjectId();
            this.bsonId = id.toString();
        }

        this.bsonIdValue.setValue(this.getValue());
    },

    initComponent: function() {
        LonelyStorm.Air.Bson.Item.superclass.initComponent.call(this);

        this.bsonIdValue = new CQ.Ext.form.Hidden({
            name: this.field.name
        });
        this.items.get(0).add(this.bsonIdValue);

        this.field.name = null;
    },

    getRawValue: function() {
        var item = {
            'bsonId': this.bsonId,
            'field': this.field.getValue()
        };

        return JSON.stringify(item);
    },

    getValue: function() {
        return this.getRawValue();
    },

    setValue: function(value) {
        var item = JSON.parse(value);

        this.bsonId = item.bsonId;
        this.field.setValue(item.field);
        this.updateBsonId();
    }

});
CQ.Ext.reg('lonelystorm.air.bson.item', LonelyStorm.Air.Bson.Item);
