package com.bot.worker;

import net.sf.oval.guard.GuardAspect;
import org.aspectj.lang.annotation.Aspect;

/**
 * OVal guard default implementation.
 * Required to init guard and perform runtime checks.
 *
 * @see <a href="http://oval.sourceforge.net/userguide.html#project-preparation">Oval project preparation</a>
 * @author Aleks
 */
@Aspect
public class DefaultGuardAspect extends GuardAspect {
    public DefaultGuardAspect() {
        super();
    }
}
