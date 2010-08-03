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

    /***************************** Stuff ******************************************************************************/
    rf.ui = rf.ui || {};

    /***************************** Constructor definition *************************************************************/
    var DEFAULT_OPTIONS = {
    };

    rf.ui.TogglePanelItem = function(componentId, options) {
        // call constructor of parent class
        $super.constructor.call(this, componentId);
        $p.attachToDom.call(this, componentId);
        this.options = $.extend({}, DEFAULT_OPTIONS, options);

        this.name = this.options.name;
        this.togglePanelId = this.options.togglePanelId;
        this.switchMode = this.options.switchMode;
    };

    // Extend component class and add protected methods from parent class to our container
    var $p = rf.BaseComponent.extend(rf.BaseComponent, rf.ui.TogglePanelItem, {});

    // define super class link
    var $super = rf.ui.TogglePanelItem.$super;

    /***************************** Private Static Methods *************************************************************/

    function fireLeave($this) {
        return rf.Event.fireById($this.id, "leave");
    }

    function fireEnter($this) {
        return rf.Event.fireById($this.id, "enter");
    }

    /***************************** Public Methods  ********************************************************************/
    $.extend(rf.ui.TogglePanelItem.prototype, (function () {
        return {
            // class name
            name:"TogglePanelItem",

            // public api
            /**
             * @methodOf
             * @name TogglePanelItem#getName
             *
             * @return {String} panel item name
             */
            getName: function () {
                return this.options.name;
            },

            /**
             * @methodOf
             * @name TogglePanelItem#getTogglePanel
             *
             * @return {TogglePanel} parent TogglePanel
             * */
            getTogglePanel : function () {
                return rf.$(this.togglePanelId);
            },

            /**
             * @methodOf
             * @name TogglePanelItem#isSelected
             *
             * @return {Boolean} true if this panel item is selected in the parent toggle panel
             * */
            isSelected : function () {
                return this.getName() == this.getTogglePanel().getSelectItem();
            },

            /**
             * @private
             *
             * used in TogglePanel
             * */
            enter : function () {
                rf.getDomElement(this.id).style.display = "block";

                return fireEnter(this);
            },

            /**
             * @private
             *
             * used in TogglePanel
             * */
            leave : function () {
                var continueProcess = fireLeave(this);
                if (!continueProcess) {
                    return false;
                }

                rf.getDomElement(this.id).style.display = "none";
                return true;
            },

            // class stuff
            destroy: function () {
                //                 rf.Event.unbindById(this.options.buttonId, "."+this.namespace);
                //                 rf.Event.unbindById(this.componentId, "."+this.namespace);
                $super.destroy.call(this);
            }
        };
    })());
})(jQuery, RichFaces);
