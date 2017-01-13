/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.alterationEffects.damageOverTime;

import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.alterationEffects.AlterationEffects;
import org.terasology.alterationEffects.OnEffectRemoveEvent;
import org.terasology.context.Context;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.delay.DelayManager;
import org.terasology.math.TeraMath;

import java.util.Map;

public class CureAllDamageOverTimeAlterationEffect implements AlterationEffect {

    private final Time time;
    private final DelayManager delayManager;
    private Context context;

    public CureAllDamageOverTimeAlterationEffect(Context context) {
        this.context = context;
        this.time = context.get(Time.class);
        this.delayManager = context.get(DelayManager.class);
    }

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration) {
        DamageOverTimeComponent dot = entity.getComponent(DamageOverTimeComponent.class);
        if (dot != null) {
            // Cure all sources of this type of DOT.
            for (Map.Entry<String, Map<String, Boolean>> dotType : dot.effectIDMap.entrySet()) {
                for (Map.Entry<String, Boolean> dotSource : dotType.getValue().entrySet()) {
                    delayManager.cancelDelayedAction(entity, AlterationEffects.EXPIRE_TRIGGER_PREFIX +
                            AlterationEffects.DAMAGE_OVER_TIME + ":" + dotType.getKey() + "|" + dotSource.getKey());
                }

                //entity.send(new OnEffectRemoveEvent(entity, entity, this, AlterationEffects.CONSUMABLE_ITEM, dotType.getKey()));

                // Send an event to remove the temporary source effects that caused this DOT effect. This is intended to
                // remove temporary effects like from potions, but not from equipment or more permanent sources.
                DamageOverTimeAlterationEffect dotAlterationEffect = new DamageOverTimeAlterationEffect(context);
                entity.send(new OnEffectRemoveEvent(entity, entity, dotAlterationEffect, AlterationEffects.CONSUMABLE_ITEM, dotType.getKey()));

                // After removing the temporary DOT effects of this type, call the DOT alteration effect to calculate the new
                // magnitude and duration.
                dotAlterationEffect.applyEffect(entity, entity, dotType.getKey(), 0, 0);
            }

            entity.removeComponent(DamageOverTimeComponent.class);
        }
    }

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, String id, float magnitude, long duration) {
        applyEffect(instigator, entity, magnitude, duration);
    }

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, String effectID, String id, float magnitude, long duration) {

    }
}
