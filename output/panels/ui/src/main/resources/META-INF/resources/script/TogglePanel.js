/*
 * JBoss, Home of Professional Open Source
 * Copyright ${year}, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

(function ($, rf) {

    rf.ui = rf.ui || {};

    /***************************** Constructor definition *************************************************************/
    var DEFAULT_OPTIONS = {
    };

    /**
     * @class TogglePanel
     * @name TogglePanel
     *
     * @constructor
     * @param {String} componentId - component id
     * @param {Hash} options - params
     * */
    rf.ui.TogglePanel = function (componentId, options) {
        // call constructor of parent class
        $super.constructor.call(this, componentId);
        $p.attachToDom.call(this, componentId);
        this.options = $.extend({}, DEFAULT_OPTIONS, options);

        this.selectedItem = this.options.selectedItem;
        this.switchMode = this.options.switchMode;
        this.items = this.options.items;
    };

    var $p = {};

    // Extend component class and add protected methods from parent class to our container
    $p = rf.BaseComponent.extend(rf.BaseComponent, rf.ui.TogglePanel, $p);

    var $super = rf.ui.TogglePanel.$super;

    /***************************** Private Static Area ****************************************************************/


    /* SIMPLE INNER CLASS for handle switch operation*/
    function SwitchItems (comp) {
        this.comp = comp;
    }

    SwitchItems.prototype = {

        /**
         * @param {TogglePanel} comp
         * @param {TogglePanelItem} oldPanel
         * @param {TogglePanelItem} newPanel
         *
         * @return {void}
         * */
        exec : function (oldPanel, newPanel) {
            if (this.comp.switchMode == "server") {
                return this.execServer(oldPanel, newPanel);
            } else if (this.comp.switchMode == "ajax") {
                return this.execAjax(oldPanel, newPanel);
            } else if (this.comp.switchMode == "client") {
                return this.execClient(oldPanel, newPanel);
            } else {
                rf.log.error("SwitchItems.exec : unknown switchMode (" + this.comp.switchMode + ")");
            }
        },

        /**
         * @protected
         * @param {TogglePanelItem} oldPanel
         * @param {TogglePanelItem} newPanel
         *
         * @return {Boolean} false
         * */
        execServer : function (oldPanel, newPanel) {
            var continueProcess = oldPanel.leave();
            if (!continueProcess) {
                
                return false;
            }

            this.setSelectedItem(newPanel.getName());

            rf.submitForm(this.getParentForm(), null, {});

            return false;
        },

        /**
         * @protected
         * @param {TogglePanelItem} oldPanel
         * @param {TogglePanelItem} newPanel
         *
         * @return {Boolean} false
         * */
        execAjax : function (oldPanel, newPanel) {
            var options = $.extend({}, this.comp.options["ajax"], {}/*this.getParameters(newPanel)*/);

            this.setSelectedItem(newPanel.getName());
            rf.ajax(this.comp.id, null, options);
            this.setSelectedItem(oldPanel.getName());

            return false;
        },

        /**
         * @protected
         * @param {TogglePanelItem} oldPanel
         * @param {TogglePanelItem} newPanel
         *
         * @return {undefined}
         *             - false - if process has been terminated
         *             - true  - in other cases
         * */
        execClient : function (oldPanel, newPanel) {
            var continueProcess = oldPanel.leave();
            if (!continueProcess) {
                return false;
            }

            this.setSelectedItem(newPanel.getName());

            newPanel.enter();
            fireItemChange(this.comp, oldPanel, newPanel);

            return true;
        },

        /**
         * @private
         * */
        getParentForm : function () {
            return $(RichFaces.getDomElement(this.comp.id)).parent('form');
        },

        /**
         * @private
         * */
        setSelectedItem : function (name) {
            rf.getDomElement(this.getValueInputId()).value = name;
            this.comp.selectedItem = name;
        },

        /**
         * @private
         * */
        getValueInputId: function () {
            return this.comp.id + "-value"
        }

    };

    /********************* Events *************************/

    /**
     * Fire Concealable Event
     * */
    function fireBeforeItemChange ($this, oldItem, newItem) {
        return rf.Event.fireById($this.id, "beforeitemchange", {
            id: $this.id,
            oldItem : oldItem,
            newItem : newItem
        });
    }

    function fireItemChange ($this, oldItem, newItem) {
        return new rf.Event.fireById($this.id, "itemchange", {
            id: $this.id,
            oldItem : oldItem,
            newItem : newItem
        });
    }

    /********************* Methods *************************/

    var ITEMS_META_NAMES = {
        "@first" : function (comp) { return 0; },
        "@prev"  : function (comp) { return getItemIndex(comp.items, comp.selectedItem) - 1; },
        "@next"  : function (comp) { return getItemIndex(comp.items, comp.selectedItem) + 1; },
        "@last"  : function (comp) { return comp.items.length - 1; }
    };    

    function getItemIndex (items, itemName) {
        for (var i = 0; i < items.length; i++) {
            if (items[i].getName() === itemName) {
                return i;
            }
        }

        rf.log.info("TogglePanel.getItemIndex: item with name '" + itemName + "' not found");
        return -1;
    }

    /**
     * @param {TogglePanelItem[]} items
     * @param {Number} index - array index
     *
     * @return {TogglePanelItem}
     *    null - if item not found
     * */
    function getItem (items, index) {
        if (index >= 0 && index < items.length) {
            return items[index]
        }

        return null;
    }

    function getItemByName (items, name) {
        return getItem(items, getItemIndex(items, name));
    }

    function getItemName (items, index) {
        var item = getItem(items, index);
        if (item == null) {
            return null;
        }
        
        return item.getName();
    }


    /***************************** Public Methods  ********************************************************************/
    $.extend(rf.ui.TogglePanel.prototype, (function () {
        return {
            // class name
            name:"TogglePanel",

            /**
             * @methodOf
             * @name TogglePanel#getSelectItem
             *
             * @return {String} name of current selected panel item
             */
            getSelectItem: function () {
                return this.selectedItem;
            },

            /**
             * @methodOf
             * @name TogglePanel#switchToItem
             *
             * @param {String} name - panel item name to switch
             *           we can use meta names @first, @prev, @next and @last
             * @return {Boolean} - false if something wrong and true if all is ok
             */
            switchToItem: function (name) {
                var newPanel = this.getNextItem(name);
                if (newPanel == null) {
                    rf.log.warn("TogglePanel.switchToItems(" + name + "): item with name '" + name + "' not found");
                    return false;
                }

                var oldPanel = getItemByName(this.items, this.getSelectItem());

                var continueProcess = fireBeforeItemChange(this, oldPanel, newPanel);
                if (!continueProcess) {
                    rf.log.warn("TogglePanel.switchToItems(" + name + "): switch has been canceled by beforeItemChange event");
                    return false
                }

                return new SwitchItems(this).exec(oldPanel, newPanel);
            },

            /**
             * @methodOf
             * @name TogglePanel#getNextItem
             *
             * @param {String} name of TogglePanelItem or meta name (@first | @prev | @next | @last) 
             * @return {TogglePanelItem} null if item not found
             */
            getNextItem : function (name) {
                if (name) {
                    var newItemIndex = ITEMS_META_NAMES[name];
                    if (newItemIndex) {
                        return getItem(this.items, newItemIndex(this));
                    } else {
                        return getItemByName(this.items, name);
                    }
                } else {
                    return getItemByName(this.items, this.nextItem());
                }
            },

            /**
             * please, remove this method when client side ajax events will be added
             * 
             * */
            onCompleteHandler : function (oldItemName, newItemName) {
                var oldItem = getItemByName(this.items, oldItemName);
                var newItem = getItemByName(this.items, newItemName);

                // Don't do like this and remove it ASAP
                new SwitchItems(this).execClient(oldItem, newItem);
            },

            /**
             * @methodOf
             * @name TogglePanel#getItems
             *
             * @return {TogglePanelItem[]} all defined panel items
             */
            getItems : function () {
                return this.items;
            },

            /**
             * @methodOf
             * @name TogglePanel#getItemsNames
             *
             * @return {String[]} names of all defined items
             */
            getItemsNames: function () {
                var res = [];
                for (var item in this.items) {
                    res.push(this.items[item].getName());
                }

                return res;
            },

            /**
             * @methodOf
             * @name TogglePanel#nextItem
             *
             * @param {String} [itemName = selectedItem]
             * @return {String} name of next panel item
             */
            nextItem: function (itemName) {
                var itemIndex = getItemIndex(this.items, itemName || this.selectedItem);
                if (itemIndex == -1) {
                    return null;
                }

                return getItemName(this.items, itemIndex + 1);
            },

            /**
             * @methodOf
             * @name TogglePanel#firstItem
             *
             * @return {String} name of first panel item
             */
            firstItem: function () {
                return getItemName(this.items, 0);
            },

            /**
             * @methodOf
             * @name TogglePanel#lastItem
             *
             * @return {String} name of last panel item
             */
            lastItem: function () {
                return getItemName(this.items, this.items.length - 1);
            },

            /**
             * @methodOf
             * @name TogglePanel#prevItem
             *
             * @param {String} itemName
             * @return {String} name of prev panel item
             *                  null if it is first item
             */
            prevItem: function (itemName) {
                var itemIndex = getItemIndex(this.items, itemName || this.selectedItem);
                if (itemIndex < 1) {
                    return null;
                }
                
                return getItemName(this.items, itemIndex - 1);
            },

            // class stuff
            destroy: function () {
                //                 rf.Event.unbindById(this.options.buttonId, "."+this.namespace);
                //                 rf.Event.unbindById(this.componentId, "."+this.namespace);
                //                 $super.destroy.call(this);
            }
        };
    })());
})(jQuery, RichFaces);
