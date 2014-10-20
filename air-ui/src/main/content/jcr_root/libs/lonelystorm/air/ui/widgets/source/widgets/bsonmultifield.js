CQ.Ext.namespace('LonelyStorm.Air.Bson');

LonelyStorm.Air.Bson.MultiField = CQ.Ext.extend(CQ.form.MultiField, {

    activationField: null,

    constructor: function(config) {
        config = CQ.Util.applyDefaults(config, {
            'defaults': {
                'xtype': 'lonelystorm.air.bson.item'
            },
            'type': 'foundation/components/parsys'
        });

        LonelyStorm.Air.Bson.MultiField.superclass.constructor.call(this, config);

        var dialog = this.findParentByType('dialog');
        dialog.on('beforesubmit', function() {
            this.items.each(function() {
                if (this.xtype == config.defaults.xtype) {
                    if (typeof this.updateBsonId == 'function') {
                        this.updateBsonId.call(this);
                    }
                }
            });
        }, this);

        this.activationField = new CQ.Ext.form.Hidden({
            'name': ":lonelystorm.air:bson@" + config.name,
            'value': config.type
        });
        this.items.get(0).add(this.activationField);
    }

});
CQ.Ext.reg('lonelystorm.air.bson.multifield', LonelyStorm.Air.Bson.MultiField);
